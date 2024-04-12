package ru.otus.dao;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import ru.otus.config.TestFileNameProvider;
import ru.otus.dao.dto.QuestionDto;
import ru.otus.domain.Question;
import ru.otus.exceptions.QuestionReadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        try (InputStream file = getInputStream()) {
            return readQuestionsFromCsv(file);
        } catch (IOException e) {
            throw new QuestionReadException("Error reading question file", e);
        }
    }

    private InputStream getInputStream() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileNameProvider.getTestFileName());
        return Objects.requireNonNull(inputStream);
    }

    private List<Question> readQuestionsFromCsv(InputStream file) {
        List<Question> questions;

        try (InputStreamReader streamReader = new InputStreamReader(file, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(streamReader)) {

            CsvToBean<QuestionDto> csvToBean = buildCsvToBean(bufferedReader);
            List<QuestionDto> questionDtos = csvToBean != null ? csvToBean.parse() : null;

            if (questionDtos != null && questionDtos.isEmpty()) {
                throw new QuestionReadException("No questions were found!", new RuntimeException());
            }

            questions = questionDtos != null ? questionDtos.stream()
                    .map(QuestionDto::toDomainObject)
                    .collect(Collectors.toList()) : null;

        } catch (IOException e) {
            throw new QuestionReadException("Error while parsing CSV file", e);
        }

        return questions;
    }

    private CsvToBean<QuestionDto> buildCsvToBean(BufferedReader bufferedReader) {
        return new CsvToBeanBuilder<QuestionDto>(bufferedReader)
                .withSkipLines(1)
                .withSeparator(';')
                .withType(QuestionDto.class)
                .build();
    }
}
