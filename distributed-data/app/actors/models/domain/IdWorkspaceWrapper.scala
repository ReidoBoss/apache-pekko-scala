package actors
package models
package domain

import java.util.UUID

import play.api.libs.json.*

import _root_.models.domain.IdWorkspace

case class IdWorkspaceWrapper(
    idWorkspace: IdWorkspace
) {
  def value: IdWorkspace = idWorkspace
}

object IdWorkspaceWrapper {
  given Reads[IdWorkspaceWrapper] = new Reads[IdWorkspaceWrapper] {
    def reads(json: JsValue): JsResult[IdWorkspaceWrapper] = for {
      idWorkspace <- (json \ "idWorkspace").validate[UUID]
    } yield IdWorkspaceWrapper(IdWorkspace(idWorkspace))

  }
}
