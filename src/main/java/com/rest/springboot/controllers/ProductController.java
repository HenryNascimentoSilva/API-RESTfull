package com.rest.springboot.controllers;

import com.rest.springboot.dtos.ProductRecordDto;
import com.rest.springboot.models.ProductModel;
import com.rest.springboot.repositories.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ProductController {
    @Autowired
    ProductRepository productRepository;

    // mapeamento da uri
    @PostMapping("/products")
    // ResponseEntity(que retorna um response http) que recebe uma lista do produto
    // @RequestBody para requistar um corpo e @Valid para validar
    // var para inicializar o ProductModel
    // copyProperties recebe dois parametros: o primeiro é o parametro a ser copiado o segundo é o receptor
    // melhor que usar Getters e Setters
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto){
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @GetMapping("/products")
    // Primeiro transformamos os produtos em lista
    // e usamos o metodo findAll() do produtoRepository que pega todos os "produtos" dentro do banco de dados
    // if para verificar se a lista está vazia, se não, retorna um metodo HateOAS
    // primeiro definimos o id com o metódo Getter chamado getIdProduct()
    // e então usamos add para adicionar na lista que foi criada um linkTo(que sao os links)
    // methodOn(que recebe como parametro o controller onde esta esse metodo e o metodo em si que no caso é o getOneProduct)
    // SelfRel não importa
    public ResponseEntity<List<ProductModel>> getAllProducts(){
        List<ProductModel> products = productRepository.findAll();
        if(!products.isEmpty()){
            for(ProductModel product : products){
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable(value = "id") UUID id){
        Optional<ProductModel> product = productRepository.findById(id);

        if(product.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        // não esquecar de usar .get() para transformar o optional em lista
        product.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withSelfRel());
        return ResponseEntity.status(HttpStatus.OK).body(product.get());
    }
    
    @PutMapping("/products/id")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id,
                                                @RequestBody @Valid ProductRecordDto productRecordDto){
        Optional<ProductModel> product = productRepository.findById(id);

        if(product.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }

        var productModel = product.get();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.OK).body(productModel);
    }

    @DeleteMapping("/products/id")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") UUID id){
        Optional<ProductModel> product = productRepository.findById(id);

        if(product.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        productRepository.delete(product.get());
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted sucessfully.");
    }
}
