package de.evoila.osb.service.registry.util;

import de.evoila.osb.service.registry.exceptions.InvalidFieldException;
import de.evoila.osb.service.registry.exceptions.ParseLinkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.mapping.LinkCollector;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HateoasBuilder {

    private static Logger log = LoggerFactory.getLogger(HateoasBuilder.class);

    private LinkCollector linkCollector;

    public HateoasBuilder(LinkCollector linkCollector) {
        this.linkCollector = linkCollector;
    }

    public <T> Resource<T> buildResource(T object) {
        Resource<T> resource = new Resource<>(object);
        resource.add(getLinksForObject(object));
        return resource;
    }

    public Links getLinksForObject(Object o) {
        try {
            return linkCollector.getLinksFor(o);
        } catch(IllegalArgumentException ex) {
            log.error("Creating a link failed due to an IllegalArgumentException. This can be caused by a null value.", ex);
        }
        return new Links();
    }

    /**
     * Return the links for collection of objects:
     * * selfref link
     * * profile link
     *
     * @param controllerClass class of the controller to base the links on
     * @param objectName name for the collection to refer to
     * @return Links object with selfref and profile link
     */
    public Links getLinksForCollection(Class controllerClass, String objectName) {
        if (controllerClass == null || StringUtils.isEmpty(objectName)) return new Links();
        Link selfLink = ControllerLinkBuilder.linkTo(controllerClass).slash(objectName).withSelfRel();
        Link profileLink = ControllerLinkBuilder.linkTo(controllerClass).slash("profile").slash(objectName).withRel("profile");
        return new Links(selfLink, profileLink);
    }

    /**
     * Extracts the id after the given object name from an url path.
     * Example:  http://127.0.0.1:8080/contexts/0123456789
     * Result: 0123456789
     *
     * @param objectName
     * @param link
     * @return
     * @throws ParseLinkException
     */
    public String getIdAfterObjectInLink(String objectName, Link link) throws ParseLinkException {
        if (link == null) return null;
        String href = link.getHref();
        if (!href.contains(objectName)) throw new ParseLinkException("Link does not contain requested object path.");
        try {
            String id = href.substring(href.indexOf(objectName)+ objectName.length() + 1 );
            if (id.contains("/"))
                id = id.substring(0, id.indexOf("/"));
            id = id.trim();
            if (!IdService.verifyId(id)) throw new InvalidFieldException("id");
            return id;
        } catch (IndexOutOfBoundsException ex) {
            throw new ParseLinkException("Link has not all necessary information -> tried to access invalid index of link.", ex);
        } catch (InvalidFieldException e) {
            throw new ParseLinkException("Extracted id has special characters.");
        }
    }
}
