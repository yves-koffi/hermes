package store.purchase.infrastructure.api;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.executable.ValidateOnExecution;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import store.purchase.application.usecase.CreateProductUseCase;
import store.purchase.application.command.UpdateProductCommand;
import store.purchase.application.usecase.FindAllProductsUseCase;
import store.purchase.application.usecase.FindProductByExternalProductIdUseCase;
import store.purchase.application.usecase.UpdateProductUseCase;
import store.purchase.infrastructure.api.dto.CreateProductRequest;
import store.purchase.infrastructure.api.dto.ProductSummaryDto;
import store.purchase.infrastructure.api.dto.ProduitDetailsDto;
import store.purchase.infrastructure.api.dto.UpdateProductRequest;
import store.purchase.infrastructure.api.mapper.ProductApiMapper;

import java.util.List;

@Path("/products")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final FindAllProductsUseCase findAllProductsUseCase;
    private final FindProductByExternalProductIdUseCase findProductByExternalProductIdUseCase;
    private final ProductApiMapper productApiMapper;

    @Inject
    public ProductResource(
            CreateProductUseCase createProductUseCase,
            UpdateProductUseCase updateProductUseCase,
            FindAllProductsUseCase findAllProductsUseCase,
            FindProductByExternalProductIdUseCase findProductByExternalProductIdUseCase,
            ProductApiMapper productApiMapper
    ) {
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.findAllProductsUseCase = findAllProductsUseCase;
        this.findProductByExternalProductIdUseCase = findProductByExternalProductIdUseCase;
        this.productApiMapper = productApiMapper;
    }

    @POST
    public Uni<ProduitDetailsDto> create(@Valid CreateProductRequest request) {
        return createProductUseCase.handle(productApiMapper.toCreateCommand(request))
                .map(productApiMapper::toDetails);
    }

    @PUT
    @Path("/{externalProductId}")
    public Uni<ProduitDetailsDto> update(
            @PathParam("externalProductId")
            @NotBlank(message = "externalProductId is required") String externalProductId,
            @Valid UpdateProductRequest request
    ) {
        return updateProductUseCase.handle(withExternalProductId(externalProductId, productApiMapper.toUpdateCommand(request)))
                .map(productApiMapper::toDetails);
    }

    @GET
    public Uni<List<ProductSummaryDto>> findAll() {
        return findAllProductsUseCase.handle()
                .map(productApiMapper::toSummaryList);
    }

    @GET
    @Path("/{externalProductId}")
    public Uni<Response> findByExternalProductId(
            @PathParam("externalProductId")
            @NotBlank(message = "externalProductId is required") String externalProductId
    ) {
        return findProductByExternalProductIdUseCase.handle(externalProductId)
                .map(product -> product
                        .map(value -> Response.ok(productApiMapper.toDetails(value)).build())
                        .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build()));
    }

    private UpdateProductCommand withExternalProductId(String externalProductId, UpdateProductCommand command) {
        return new UpdateProductCommand(
                command.id(),
                command.type(),
                command.platform(),
                externalProductId,
                command.packageName(),
                command.bundleId(),
                command.appAppleId()
        );
    }
}
