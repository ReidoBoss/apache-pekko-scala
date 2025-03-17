package object actors {

  import java.util.UUID

  opaque type IdUser = UUID
  object IdUser {
    def apply(x: UUID): IdUser        = x
    def fromString(x: String): IdUser = IdUser(UUID.fromString(x))
  }
  opaque type IdWorkspace = UUID
  object IdWorkspace {
    def apply(x: UUID): IdWorkspace        = x
    def fromString(x: String): IdWorkspace = IdWorkspace(UUID.fromString(x))
  }
}
