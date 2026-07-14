package com.ai.interaction.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RAGService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    // Dummy documents for our knowledge base
    private static final List<String> PHARMACY_DOCUMENTS = List.of(
            "Our mail-order pharmacy offers free standard shipping on all orders over $50. Expedited shipping is available for an additional fee.",
            "To refill a prescription, please visit our website and log into your account. Navigate to 'My Prescriptions' and select the medication you wish to refill.",
            "Ibuprofen is a nonsteroidal anti-inflammatory drug (NSAID) used for pain relief, fever reduction, and inflammation. Common side effects include stomach upset and headache.",
            "Amoxicillin is a penicillin antibiotic used to treat a variety of bacterial infections. It should be taken as directed by your doctor and can cause nausea or diarrhea.",
            "Our customer support is available Monday to Friday, 9 AM to 5 PM EST. You can reach us by phone at 1-800-PHARMACY or via email at support@mailorderpharmacy.com.",
            "We accept most major insurance plans. Please provide your insurance details during checkout or update them in your profile.",
            "For urgent medical advice, always contact your doctor or a local emergency service. Our AI assistant cannot provide medical diagnoses or personalized treatment recommendations."
    );

    public RAGService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
       this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        loadDocuments();
    }

    private void loadDocuments() {
        List<Document> documents = PHARMACY_DOCUMENTS.stream()
                .map(content -> new Document(content))
                .collect(Collectors.toList());
        vectorStore.add(documents);
        System.out.println("RAGService: Loaded " + documents.size() + " documents into the vector store.");
    }

    public String ragQuery(String userQuery) {
        // Step 1: Query Rewriting/Expansion
        // Use the LLM to generate an optimized search query from the user's natural language query
        String optimizedSearchQuery = chatClient.prompt()
                .user(p -> p.text("""
                    Rewrite the following user question into an optimized search query for a document retrieval system.
                    Focus on keywords and concepts that would best match relevant documents.
                    User Question: {userQuery}
                    Optimized Search Query:
                    """))
                .call()
                .content();

        System.out.println("RAGService: Original Query: '" + userQuery + "' -> Optimized Search Query: '" + optimizedSearchQuery + "'");

        // Step 2: Enhanced Retrieval using the optimized query
        List<Document> relevantDocuments = vectorStore.similaritySearch(SearchRequest.builder().query(optimizedSearchQuery).topK(4).build());

        // Step 3: Contextual Augmentation with Synthesis/Re-ranking Instruction
        String context = relevantDocuments.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n---\n"));

        String augmentedPrompt = String.format("""
                You are a helpful and knowledgeable AI assistant for a mail-order pharmacy.
                Your task is to answer the user's question comprehensively and accurately.
                Prioritize information from the provided context documents.
                If the answer is not explicitly available or cannot be logically inferred from the context,
                state that you cannot answer from the provided information.
                Do not make up information.

                Original User Question: %s
                Optimized Search Query Used: %s

                Context Documents:
                %s

                Your Answer:
                """, userQuery, optimizedSearchQuery, context);

        // Step 4: Final LLM Call with augmented prompt
        return chatClient.prompt()
                .user(augmentedPrompt)
                .call()
                .content();
    }
}
