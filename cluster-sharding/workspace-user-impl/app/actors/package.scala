package object actors {

  import java.util.UUID

  opaque type IdUser = UUID
  object IdUser {
    def apply(x: UUID): IdUser = x
  }
  opaque type IdWorkspace = UUID
  object IdWorkspace {
    def apply(x: UUID): IdWorkspace = x
  }
}
