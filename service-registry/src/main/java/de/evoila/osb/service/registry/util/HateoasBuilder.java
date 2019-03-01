package de.evoila.osb.service.registry.util;

import org.springframework.data.rest.webmvc.mapping.LinkCollector;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HateoasBuilder {

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
        return linkCollector.getLinksFor(o);
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
}
