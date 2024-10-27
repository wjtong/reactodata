# reactodata
This project is based on Quarkus. It is using Olingo to provide OData V4 services.  
The OData services are defined in config folder. The files are in json format.
Here is the example:
```json
{
    "service-name": "PipelineManage",
    "name-space": "com.banfftech",
    "entity-types": [
      {
        "entity-name": "Connector"
      },
      {
        "entity-name": "Dataset"
      },
      {
        "entity-name": "Workspace"
      },
      {
        "entity-name": "DatasetSpace"
      }
    ]
}
```
This file defined an OData service named PipelineManage. The entity types that defined in this service are Connector, Dataset, Workspace and DatasetSpace. These entity types have corresponding model classes defined in com.banfftech.reactodata.model package. The fields in model class will be the Properties of the corresponding entity type.