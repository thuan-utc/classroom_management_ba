package utc.k61.cntt2.class_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import utc.k61.cntt2.class_management.domain.ClassDocument;
import utc.k61.cntt2.class_management.service.DocumentService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/document")
public class ClassDocumentController {
    private final DocumentService documentService;

    @Autowired
    public ClassDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam Map<String, String> params, Pageable pageable) throws Exception {
        Page<ClassDocument> page = documentService.search(params, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping
    public ResponseEntity<?> search(@RequestParam Long classId) throws Exception {
        List<ClassDocument> list = documentService.search(classId);
        return ResponseEntity.ok(new PageImpl<>(list));
    }



}
