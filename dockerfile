# Use the official .NET SDK image to build the application
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the csproj file and restore any dependencies (via nuget)
COPY *.csproj ./
RUN dotnet restore

# Copy the rest of the application files
COPY . ./

# Publish the application to the /out directory
RUN dotnet publish -c Release -o /out

# Use the official .NET runtime image to run the application
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime

# Set the working directory for the runtime container
WORKDIR /app

# Copy the published application from the build stage
COPY --from=build /out .

# Expose the port the app will listen on
EXPOSE 8080

# Set the entrypoint to run the application
ENTRYPOINT ["dotnet", "BasicDotNetApi.dll"]
