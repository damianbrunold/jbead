#pragma once

#include "memento.h"

namespace jbead {

/*  .jbb format reader/writer. The on-disk representation is the
    pretty-printed S-expression produced by ObjectModel::toString();
    structure is documented inline in jbeadmemento.cpp::save.     */
class JBeadMemento : public Memento
{
public:
    static constexpr int VERSION = 1;

    void save(QIODevice* out) const override;
    void load(QIODevice* in)  override;
};

} // namespace jbead
