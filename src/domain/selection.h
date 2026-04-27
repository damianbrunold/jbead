#pragma once

#include "beadpoint.h"
#include "beadrect.h"

#include <QObject>

#include <cstdlib>

namespace jbead {

/*  Mutable rectangular selection. Composes BeadRect rather than
    inheriting from it (the legacy Java Selection extends Rect; in
    C++ we keep the rect as a member to avoid a QObject + Rect base
    diamond). The two listener events selectionUpdated /
    selectionDeleted become Qt signals.                          */
class Selection : public QObject
{
    Q_OBJECT
public:
    explicit Selection(QObject* parent = nullptr);

    BeadPoint origin()      const { return m_begin; }
    BeadPoint destination() const { return m_end; }

    int  deltaX() const { return m_end.x() - m_begin.x(); }
    int  deltaY() const { return m_end.y() - m_begin.y(); }
    int  dx() const { return m_begin.x() < m_end.x() ? 1 : -1; }
    int  dy() const { return m_begin.y() < m_end.y() ? 1 : -1; }

    bool isActive() const { return m_active; }
    bool isNormal() const
    {
        return m_active && m_begin.x() != m_end.x() && m_begin.y() != m_end.y();
    }

    BeadRect rect() const { return BeadRect(m_begin, m_end); }

    /*  Diagonal selections snap to a 45-degree line constrained by
        the shorter axis — used by the line-drawing tool when the
        user drags off-axis with shift held.                       */
    BeadPoint lineDestination() const;

    void init(BeadPoint origin);
    void update(BeadPoint end);
    void clear();

signals:
    /*  Emitted whenever begin / end / active changes. Carries before
        and after rects so a listener can invalidate just the
        symmetric-difference area.                                  */
    void selectionUpdated(BeadRect before, BeadRect current, bool wasActive, bool nowActive);
    void selectionDeleted(BeadRect rect);

private:
    BeadPoint m_begin;
    BeadPoint m_end;
    bool      m_active = false;
};

} // namespace jbead
