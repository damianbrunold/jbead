namespace JBead.Web.Core;

/// Single source of truth for z-index ordering across modals and context menus.
/// Each time a dialog opens it Acquires a number (strictly above anything currently
/// open); Release on close. Last-opened always paints on top, which is what users
/// expect when a context menu is triggered from inside a modal.
public class DialogStack
{
    private const int BaseZ = 10000;
    private const int Step = 10;
    private int count;

    public int Acquire()
    {
        count++;
        return BaseZ + count * Step;
    }

    public void Release()
    {
        if (count > 0) {
			count--;
		}
	}
}
