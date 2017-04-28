/*
 * OpenEdge DB plugin for SonarQube
 * Copyright (C) 2013-2016 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.web;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.RequestHandler;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.utils.text.JsonWriter;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;

public class OpenEdgeWebService implements WebService {
  private final OpenEdgeComponents components;

  public OpenEdgeWebService(OpenEdgeComponents components) {
    this.components = components;
  }

  @Override
  public void define(Context context) {
    NewController controller = context.createController("api/openedge").setDescription("OpenEdge plugin web service");
    controller.createAction("licences").setDescription("Licences list").setHandler(new LicenceRequestHandler());
    controller.done();
  }

  private class LicenceRequestHandler implements RequestHandler {

    @Override
    public void handle(Request request, Response response) throws Exception {
      JsonWriter writer = response.newJsonWriter().beginObject().name("licences").beginArray();
      for (Licence lic : components.getLicences()) {
        writer.beginObject()
          .prop("permanentId", lic.getPermanentId()).prop("customer", lic.getCustomerName()).prop("repository",
              lic.getRepositoryName()).prop("type", lic.getType().name()).prop("expiration",
                  LocalDateTime.ofEpochSecond(lic.getExpirationDate() / 1000, 0, ZoneOffset.UTC).format(
                      DateTimeFormatter.ISO_LOCAL_DATE_TIME)).endObject();
      }
      writer.endArray().endObject().close();
    }
  }

}
