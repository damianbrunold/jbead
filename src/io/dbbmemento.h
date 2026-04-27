#pragma once

#include "memento.h"

namespace jbead {

/*  Legacy DB-BEAD binary format. Capped at 10 colors and a fixed
    25000-byte payload; height is reconstructed on load as
    ceil(25000 / width). Colors past the 10-entry palette are
    discarded silently on save (legacy fills with white). The .dbb
    reader is used in production; the writer is kept for parity but
    new files should always be saved as .jbb.                      */
class DbbMemento : public Memento
{
public:
    static constexpr int DBB_FIELD_SIZE = 25000;

    int  maxSupportedColors() const override { return 10; }
    bool requiresCompactification() const override { return true; }

    void save(QIODevice* out) const override;
    void load(QIODevice* in)  override;
};

} // namespace jbead
