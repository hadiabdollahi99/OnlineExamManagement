package ir.maktabsharif.onlineexammanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "multiple_choice_questions")
@PrimaryKeyJoinColumn(name = "question_id")
public class MultipleChoiceQuestion extends BaseQuestion {

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();

    private Boolean shuffleOptions = true;

    public void addOption(QuestionOption option) {
        if (this.options == null){
            this.options = new ArrayList<>();
        }

        this.options.add(option);

        if (option != null){
            option.setQuestion(this);
        }
    }

    public void removeOption(QuestionOption option) {
        this.options.remove(option);
        option.setQuestion(null);
    }
}