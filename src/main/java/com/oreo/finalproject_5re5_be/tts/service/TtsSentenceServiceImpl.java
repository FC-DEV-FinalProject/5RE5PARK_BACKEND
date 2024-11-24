package com.oreo.finalproject_5re5_be.tts.service;

import com.oreo.finalproject_5re5_be.global.constant.BatchProcessType;
import com.oreo.finalproject_5re5_be.global.exception.EntityNotFoundException;
import com.oreo.finalproject_5re5_be.project.entity.Project;
import com.oreo.finalproject_5re5_be.project.repository.ProjectRepository;
import com.oreo.finalproject_5re5_be.tts.dto.request.TtsAttributeInfo;
import com.oreo.finalproject_5re5_be.tts.dto.request.TtsSentenceBatchInfo;
import com.oreo.finalproject_5re5_be.tts.dto.request.TtsSentenceBatchRequest;
import com.oreo.finalproject_5re5_be.tts.dto.request.TtsSentenceRequest;
import com.oreo.finalproject_5re5_be.tts.dto.response.SentenceInfo;
import com.oreo.finalproject_5re5_be.tts.dto.response.TtsSentenceDto;
import com.oreo.finalproject_5re5_be.tts.dto.response.TtsSentenceListDto;
import com.oreo.finalproject_5re5_be.tts.entity.*;
import com.oreo.finalproject_5re5_be.tts.exception.TtsSentenceInValidInput;
import com.oreo.finalproject_5re5_be.tts.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;


@Slf4j
@Service
@Validated
public class TtsSentenceServiceImpl implements TtsSentenceService {

    private final TtsSentenceRepository ttsSentenceRepository;
    private final ProjectRepository projectRepository;
    private final TtsAudioFileRepository ttsAudioFileRepository;
    private final VoiceRepository voiceRepository;
    private final StyleRepository styleRepository;
    private final TtsProgressStatusRepository ttsProgressStatusRepository;

    public TtsSentenceServiceImpl(TtsSentenceRepository ttsSentenceRepository, ProjectRepository projectRepository, TtsAudioFileRepository ttsAudioFileRepository, VoiceRepository voiceRepository, StyleRepository styleRepository, TtsProgressStatusRepository ttsProgressStatusRepository) {
        this.ttsSentenceRepository = ttsSentenceRepository;
        this.projectRepository = projectRepository;
        this.ttsAudioFileRepository = ttsAudioFileRepository;
        this.voiceRepository = voiceRepository;
        this.styleRepository = styleRepository;
        this.ttsProgressStatusRepository = ttsProgressStatusRepository;
    }

    @Override
    public TtsSentenceDto addSentence(@Valid @NotNull Long projectSeq, @Valid TtsSentenceRequest createRequest) {
        // 1. TtsSentenceRequest 유효성 검증 : Text (not blank) => @NotBlank

        // 2.1 projectSeq 유효성 검증 : not null => @NotNull
        // 2.2. projectSeq : 조회 가능한 projectSeq (존재 여부) 검증 및 할당
        Project project = projectRepository.findById(projectSeq)
                .orElseThrow(() -> new IllegalArgumentException("projectSeq is invalid"));

        // 3.1 TtsSentenceRequest.voiceSeq 유효성 검증 : not null => @NotNull
        // 3.2 voiceSeq : 조회 가능한 voiceSeq (존재 여부) 검증 및 할당
        Voice voice = voiceRepository.findById(createRequest.getVoiceSeq())
                .orElseThrow(() -> new IllegalArgumentException("voiceSeq is invalid"));

        // 3. TtsSentenceRequest 유효성 검증 : StyleSeq
        // 3.1. styleSeq : not null 일 때 styleSeq 유효성 검증 및 할당
        Style style = null;
        if (createRequest.getStyleSeq() != null) {
            style = styleRepository.findById(createRequest.getStyleSeq())
                    .orElseThrow(() -> new IllegalArgumentException("styleSeq is invalid"));
        }

        // 4. TtsSentenceRequest -> TtsSentence 변환
        TtsAttributeInfo attribute = createRequest.getAttribute();
        TtsSentence ttsSentence = TtsSentence.builder()
                .text(createRequest.getText())
                .sortOrder(createRequest.getOrder())
                .volume(attribute.getVolume())
                .speed(attribute.getSpeed())
                .startPitch(attribute.getStPitch())
                .emotion(attribute.getEmotion())
                .emotionStrength(attribute.getEmotionStrength())
                .sampleRate(attribute.getSampleRate())
                .alpha(attribute.getAlpha())
                .endPitch(attribute.getEndPitch())
                .audioFormat(attribute.getAudioFormat())
                .project(project)
                .voice(voice)
                .build();

        // 5. TtsSentence 저장
        TtsSentence createdTtsSentence = ttsSentenceRepository.save(ttsSentence);
        log.info("[ttsSentenceEntity] save : {}", createdTtsSentence);

        // 6. TtsProgressStatus 저장
        TtsProgressStatus ttsProgressStatus = TtsProgressStatus.builder()
                .ttsSentence(createdTtsSentence)
                .progressStatus(TtsProgressStatusCode.CREATED)
                .build();
        TtsProgressStatus createdTtsProgress = ttsProgressStatusRepository.save(ttsProgressStatus);
        log.info("[ttsProgressStatus] save : {}", createdTtsProgress);

        // 6. TtsSentenceResponse 변환
        return TtsSentenceDto.of(createdTtsSentence);
    }

    /**
     * @param projectSeq
     * @param tsSeq
     * @param updateRequest
     * @return
     * @apiNote TtsSentence 엔티티 수정
     */
    @Override
    public TtsSentenceDto updateSentence(@Valid @NotNull Long projectSeq, @Valid @NotNull Long tsSeq, @Valid TtsSentenceRequest updateRequest) {
        // 1. TtsSentenceRequest 유효성 검증
        if (updateRequest == null) {
            throw new IllegalArgumentException("Update request cannot be null");
        }

        // 2. DB 유효성 검증
        // 2.1 projectSeq 조회 가능한 projectSeq (존재 여부) 검증 및 할당
        Project project = projectRepository.findById(projectSeq)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectSeq));

        // 2.2 voiceSeq 조회 가능한 voiceSeq (존재 여부) 검증 및 할당
        Voice voice = voiceRepository.findById(updateRequest.getVoiceSeq())
                .orElseThrow(() -> new EntityNotFoundException("Voice not found with id: " + updateRequest.getVoiceSeq()));

        // 2.3 styleSeq 조회 가능한 styleSeq (존재 여부) 검증 및 할당
        Style style = styleRepository.findById(updateRequest.getStyleSeq())
                .orElseThrow(() -> new EntityNotFoundException("Style not found with id: " + updateRequest.getStyleSeq()));

        // 3. TtsSentenceRequest -> TtsSentence 변환
        // 3.1 TtsSentence 엔티티 조회
        TtsSentence sentence = ttsSentenceRepository.findById(tsSeq)
                .orElseThrow(() -> new EntityNotFoundException("TtsSentence not found with id: " + tsSeq));

        // 3.2 TtsSentence 엔티티 수정
        TtsSentence updateSentence = sentence.toBuilder()
                .text(updateRequest.getText())
                .voice(voice)
                .sortOrder(updateRequest.getOrder())
                .volume(updateRequest.getAttribute().getVolume())
                .speed(updateRequest.getAttribute().getSpeed())
                .startPitch(updateRequest.getAttribute().getStPitch())
                .emotion(updateRequest.getAttribute().getEmotion())
                .emotionStrength(updateRequest.getAttribute().getEmotionStrength())
                .sampleRate(updateRequest.getAttribute().getSampleRate())
                .alpha(updateRequest.getAttribute().getAlpha())
                .endPitch(updateRequest.getAttribute().getEndPitch())
                .audioFormat(updateRequest.getAttribute().getAudioFormat())
                .ttsAudiofile(null) // 3.3 TtsSentence 에 연관된 ttsAudioFile 연결 끊기
                .build();


        // 4. TtsSentence 저장
        TtsSentence updatedSentence = ttsSentenceRepository.save(updateSentence);

        // 5. TtsSentenceResponse 변환
        return TtsSentenceDto.of(updatedSentence);
    }

    /**
     * @param projectSeq
     * @param batchRequest
     * @return
     * @apiNote TtsSentence 엔티티 Batch 저장
     */
    @Override
    public TtsSentenceListDto batchSaveSentence(@Valid @NotNull Long projectSeq, @Valid TtsSentenceBatchRequest batchRequest) {
        // 1. TtsSentenceBatchRequest.sentenceList -> TtsSentenceDto List 변환
        List<TtsSentenceBatchInfo> batchList = batchRequest.getSentenceList();

        // 2. TtsSentenceDto List 변환
        List<TtsSentenceDto> batchedList = batchList.stream()
                .map(batchInfo -> toSentenceDto(projectSeq, batchInfo))
                .toList();

        return new TtsSentenceListDto(batchedList);
    }

    // batchInfo -> sentenceDto 변환
    private TtsSentenceDto toSentenceDto(Long projectSeq, TtsSentenceBatchInfo batchInfo) {
        SentenceInfo sentenceInfo = batchInfo.getSentence();

        // 1. TtsSentenceRequest 유효성 검증
        if (sentenceInfo == null) {
            throw new TtsSentenceInValidInput("SentenceInfo is null");
        }

        // 2. sentenceInfo -> TtsSentenceRequest 변환
        TtsSentenceRequest sentenceRequest = TtsSentenceRequest.builder()
                .text(sentenceInfo.getText())
                .voiceSeq(sentenceInfo.getVoiceSeq())
                .styleSeq(sentenceInfo.getStyleSeq())
                .order(sentenceInfo.getOrder())
                .attribute(sentenceInfo.getTtsAttributeInfo())
                .build();

        // 3. BatchProcessType 에 따른 분기처리
        // 3.1 CREATE : addSentence
        if (batchInfo.getBatchProcessType() == BatchProcessType.CREATE) {
            return addSentence(projectSeq, sentenceRequest);
        }

        // 3.2 UPDATE : updateSentence
        if (batchInfo.getBatchProcessType() == BatchProcessType.UPDATE) {
            return updateSentence(projectSeq, sentenceInfo.getTsSeq(), sentenceRequest);
        }

        // 3.3 해당하는 BatchProcessType 가 없으면 예외 발생
        throw new TtsSentenceInValidInput("BatchProcessType is invalid");
    }

    // 유효한 프로젝트인지 확인
    private Project getValidProject(Long projectSeq) {
        return projectRepository.findById(projectSeq)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectSeq));
    }

    // 유효한 음성인지 확인
    private Voice getValidVoice(Long voiceSeq) {
        return voiceRepository.findById(voiceSeq)
                .orElseThrow(() -> new EntityNotFoundException("Voice not found with id: " + voiceSeq));
    }

    // 유효한 스타일인지 확인
    private Style getValidStyle(Long styleSeq) {
        return styleRepository.findById(styleSeq)
                .orElseThrow(() -> new EntityNotFoundException("Style not found with id: " + styleSeq));
    }


    @Override
    public TtsSentenceDto getSentence(Long projectSeq, Long tsSeq) {

        // 1. TtsSentence 엔티티 조회
        TtsSentence ttsSentence = ttsSentenceRepository.findById(tsSeq)
                .orElseThrow(() -> new EntityNotFoundException("TtsSentence not found with id: " + tsSeq));

        return TtsSentenceDto.of(ttsSentence);
    }

    @Override
    public TtsSentenceListDto getSentenceList(Long projectSeq) {

        // 1. Project 엔티티 조회
        Project project = projectRepository.findById(projectSeq)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectSeq));

        // 2. Project 에 연관된 TtsSentence 리스트 조회
        List<TtsSentence> ttsSentenceList = ttsSentenceRepository.findAllByProject(project);

        // 3. TtsSentenceDto 리스트 변환 및 반환
        return TtsSentenceListDto.of(ttsSentenceList);
    }

    @Override
    public boolean deleteSentence(Long projectSeq, Long tsSeq) {
        // 1. TtsSentence 엔티티 조회
        TtsSentence ttsSentence = ttsSentenceRepository.findById(tsSeq)
                .orElseThrow(EntityNotFoundException::new);

        // 2. TtsSentence 삭제 (TtsSentence 만 삭제)
        ttsSentenceRepository.delete(ttsSentence);

        // 3. 결과 반환
        return true;
    }
}
