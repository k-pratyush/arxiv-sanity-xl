class InvalidPdfException(Exception):
    def __init__(self, message, errors) -> None:
        super().__init__(message)
