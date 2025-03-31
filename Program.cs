using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

var builder = WebApplication.CreateBuilder(args);

// Register services (e.g., controllers, API services)
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(); // Adds Swagger services

var app = builder.Build();

// Configure middleware (Swagger)
if (app.Environment.IsDevelopment())
{
    app.UseSwagger(); // Use Swagger to generate API documentation
    app.UseSwaggerUI(); // Use Swagger UI to view the documentation
}

// Define a simple API endpoint
app.MapGet("/api/helloworld", () =>
{
    return Results.Ok(new { message = "Hello, World!" });
});

app.Run();
