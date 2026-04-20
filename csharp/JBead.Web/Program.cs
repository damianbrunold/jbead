using Microsoft.AspNetCore.Components.Web;
using Microsoft.AspNetCore.Components.WebAssembly.Hosting;
using JBead.Web;
using JBead.Web.Core;
using JBead.Web.Tools;

var builder = WebAssemblyHostBuilder.CreateDefault(args);
builder.RootComponents.Add<App>("#app");
builder.RootComponents.Add<HeadOutlet>("head::after");

builder.Services.AddScoped(sp => new HttpClient { BaseAddress = new Uri(builder.HostEnvironment.BaseAddress) });

// Tools are discovered via DI — adding a new tool is just one AddSingleton line.
// Registration order determines toolbar order and which tool is selected at startup.
builder.Services.AddSingleton<ITool, PencilTool>();
builder.Services.AddSingleton<ITool, FillTool>();
builder.Services.AddSingleton<ITool, FillLineTool>();
builder.Services.AddSingleton<ITool, PipetteTool>();
builder.Services.AddSingleton<ITool, SelectionTool>();
builder.Services.AddSingleton<AppState>();
builder.Services.AddSingleton<CustomBeadCatalog>();
// Scoped: a fresh dialog stack per page/SPA instance. In Blazor WASM that matches
// the client's lifetime and survives between navigations on the same page.
builder.Services.AddScoped<DialogStack>();
// Scoped so component subscriptions to LanguageChanged are tied to the SPA session.
builder.Services.AddScoped<LocalizationService>();

await builder.Build().RunAsync();
