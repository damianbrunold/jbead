namespace JBead.Web.FileFormat;

public class JBeadFileFormatException : Exception
{
    public JBeadFileFormatException(string message) : base(message) { }
    public JBeadFileFormatException(string message, Exception inner) : base(message, inner) { }
}
