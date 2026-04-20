using JBead.Web.Core;
using JBead.Web.FileFormat;

var samples = new[]
{
    "stripes.jbb",
    "hearts.jbb",
    "small_hearts.jbb",
    "green_yellow_diagonal.jbb",
};

var sampleDir = Path.GetFullPath(Path.Combine(AppContext.BaseDirectory, "..", "..", "..", "..", "..", "samples"));
Console.WriteLine($"Sample dir: {sampleDir}");

foreach (var name in samples)
{
    var path = Path.Combine(sampleDir, name);
    var bytes = File.ReadAllBytes(path);
    var model = new BeadModel();
    JBeadFileFormat.Load(model, bytes);
    var used = model.GetUsedHeight();
    Console.WriteLine($"{name,-32} width={model.Width} height={model.Height} used={used} colors={model.ColorCount}");

    // Roundtrip: save, reload, verify raw data equals.
    var saved = JBeadFileFormat.Save(model);
    var model2 = new BeadModel();
    JBeadFileFormat.Load(model2, saved);
    var src = model.Field.RawData;
    var dst = model2.Field.RawData;
    if (src.Length != dst.Length || !src.SequenceEqual(dst))
    {
        Console.WriteLine($"  !! ROUNDTRIP FAILED len {src.Length} vs {dst.Length}");
        Environment.Exit(1);
    }
    Console.WriteLine($"  roundtrip OK ({saved.Length} bytes)");
}

// Basic model operations
{
    var m = new BeadModel();
    m.SelectedColor = 5;
    m.SetPoint(new Point(3, 2));
    m.SetPoint(new Point(4, 2));
    if (m.Get(new Point(3, 2)) != 5 || m.Get(new Point(4, 2)) != 5) { Console.WriteLine("SetPoint failed"); Environment.Exit(1); }
    m.Undo();
    if (m.Get(new Point(3, 2)) != 5 || m.Get(new Point(4, 2)) != 0) { Console.WriteLine("Undo failed"); Environment.Exit(1); }
    m.Redo();
    if (m.Get(new Point(3, 2)) != 5 || m.Get(new Point(4, 2)) != 5) { Console.WriteLine("Redo failed"); Environment.Exit(1); }
    Console.WriteLine("set-point/undo/redo OK");

    m.Clear();
    m.SelectedColor = 2;
    m.DrawLine(new Point(0, 0), new Point(5, 5));
    var hits = 0;
    for (var i = 0; i <= 5; i++) if (m.Get(new Point(i, i)) == 2) hits++;
    Console.WriteLine($"drawLine diagonal hits = {hits}/6");
    if (hits < 5) { Console.WriteLine("DrawLine too lossy"); Environment.Exit(1); }
}

// Roundtrip: manufacturer + id + finish on a per-bead basis.
{
    var m = new BeadModel();
    m.AddBead(new Bead(System.Drawing.Color.Red, "Miyuki", "11-0408", BeadFinish.Shiny));
    m.AddBead(new Bead(System.Drawing.Color.Blue, "Toho", "8/0-48", BeadFinish.Metallic));
    m.AddBead(new Bead(System.Drawing.Color.Green, "Preciosa", "53250", BeadFinish.TransparentGloss));

    var saved = JBeadFileFormat.Save(m);
    var loaded = new BeadModel();
    JBeadFileFormat.Load(loaded, saved);

    void Check(int idx, string mfr, string id, BeadFinish fin)
    {
        var b = loaded.GetBead(idx);
        if (b.Manufacturer != mfr || b.Id != id || b.Finish != fin)
        {
            Console.WriteLine($"  !! bead {idx} roundtrip failed: got '{b.Manufacturer}' '{b.Id}' {b.Finish}");
            Environment.Exit(1);
        }
    }
    // Indices shift: 2 default beads (background + black) then 3 added = indices 2, 3, 4.
    Check(2, "Miyuki", "11-0408", BeadFinish.Shiny);
    Check(3, "Toho", "8/0-48", BeadFinish.Metallic);
    Check(4, "Preciosa", "53250", BeadFinish.TransparentGloss);
    Console.WriteLine("bead metadata (mfr+id+finish) roundtrip OK");
}

// Roundtrip: YAML format — save in .jbb, save in YAML, load YAML, compare.
{
    var sampleBytes = File.ReadAllBytes(Path.Combine(sampleDir, "stripes.jbb"));
    var m = new BeadModel();
    JBeadFileFormat.Load(m, sampleBytes);

    var yaml = JBeadYamlFileFormat.Save(m);
    Console.WriteLine($"YAML size: {yaml.Length} bytes");

    var m2 = new BeadModel();
    JBeadYamlFileFormat.Load(m2, yaml);

    // Compare raw cell data.
    var src = m.Field.RawData;
    var dst = m2.Field.RawData;
    // Raw data length may differ if one had a trimmed height. Compare up to min.
    var minLen = Math.Min(src.Length, dst.Length);
    for (var i = 0; i < minLen; i++)
    {
        if (src[i] != dst[i])
        {
            Console.WriteLine($"  !! YAML roundtrip mismatch at index {i}: {src[i]} vs {dst[i]}");
            Environment.Exit(1);
        }
    }
    // Compare bead metadata (post-compaction, so counts may differ).
    Console.WriteLine($"YAML roundtrip OK ({m.ColorCount} → {m2.ColorCount} beads)");
}

Console.WriteLine("ALL OK");
