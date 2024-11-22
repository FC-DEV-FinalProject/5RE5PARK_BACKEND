package com.oreo.finalproject_5re5_be.vc.service;

import com.oreo.finalproject_5re5_be.global.dto.response.AudioFileInfo;
import com.oreo.finalproject_5re5_be.project.entity.Project;
import com.oreo.finalproject_5re5_be.project.repository.ProjectRepository;
import com.oreo.finalproject_5re5_be.vc.dto.request.*;
import com.oreo.finalproject_5re5_be.vc.dto.response.*;
import com.oreo.finalproject_5re5_be.vc.entity.*;
import com.oreo.finalproject_5re5_be.vc.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Validated
@Transactional
public class VcServiceImpl implements VcService{
    private VcRepository vcRepository;
    private VcSrcFileRepository vcSrcFileRepository;
    private VcTrgFileRepository vcTrgFileRepository;
    private VcResultFileRepository vcResultFileRepository;
    private VcTextRepository vcTextRepository;
    private ProjectRepository projectRepository;
    @Autowired
    public VcServiceImpl(VcRepository vcRepository,
                         VcSrcFileRepository vcSrcFileRepository,
                         VcTrgFileRepository vcTrgFileRepository,
                         VcResultFileRepository vcResultFileRepository,
                         VcTextRepository vcTextRepository,
                         ProjectRepository projectRepository) {
        this.vcRepository = vcRepository;
        this.vcSrcFileRepository = vcSrcFileRepository;
        this.vcTrgFileRepository = vcTrgFileRepository;
        this.vcResultFileRepository = vcResultFileRepository;
        this.vcTextRepository = vcTextRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Vc SRC 파일 저장
     * @param vcSrcRequest
     * @return VcUrlResponse
     */
    @Override
    @Transactional
    public VcUrlResponse srcSave(@Valid @NotNull VcSrcRequest vcSrcRequest, Long proSeq) {

        //프로젝트 조회, 객체 생성후 저장
        //vc안에 몇개의 vcSRC가 있는지 확인하여 행순서를 넣기 위한 쿼리메서드
        Integer count = vcSrcFileRepository.countByVc_ProjectSeq(vcSrcRequest.getSeq());
        VcSrcFile src = null;
        //VC 찾기
        Vc vc = vcRepository.findById(proSeq)
                .orElseThrow(() -> new IllegalArgumentException("not found"));
        //SRC를 찾아서 있다면 1로 없다면 사이즈만큼에서 +1해서 저장
        src = VcSrcFile.builder()
                .vc(vc)
                .rowOrder(count+1)
                .fileName(vcSrcRequest.getName())
                .fileUrl(vcSrcRequest.getFileUrl())
                .fileLength(vcSrcRequest.getLength())
                .fileSize(vcSrcRequest.getSize())
                .extension(vcSrcRequest.getExtension()).build();

        //프로젝트 조회한 값과 입력한 값 저장을 하기 위한 SRC 객체 생성

        log.info("[vcService] Save src 객체 생성  : {}", src); //SRC 객체 생성 확인
        VcSrcFile save = vcSrcFileRepository.save(src);// SRC 객체 저장
        log.info("[vcService] save 확인 : {} ", save);

        return VcUrlResponse.builder()//response 객체 생성
                .seq(save.getSrcSeq())
                .url(save.getFileUrl())
                .build();
    }
    @Override
    @Transactional
    public List<VcUrlResponse> srcSave(@Valid @NotNull List<VcSrcRequest> vcSrcRequests, Long proSeq) {
        List<VcUrlResponse> srcUrl = new ArrayList<>();
        for (VcSrcRequest vcSrcRequest : vcSrcRequests) {
            VcSrcFile src = null;
            //프로젝트 조회, 객체 생성후 저장
            Vc vc = projectFind(proSeq);
            //vc안에 몇개의 vcSRC가 있는지 확인하여 행순서를 넣기 위한 쿼리메서드
            Integer count = vcSrcFileRepository.countByVc_ProjectSeq(vc.getProjectSeq());
            log.info("List<VcUrlResponse> srcSave : {}",count);
            src = VcSrcFile.builder()
                    .vc(vc)
                    .rowOrder(count+1)
                    .fileName(vcSrcRequest.getName())
                    .fileUrl(vcSrcRequest.getFileUrl())
                    .fileLength(vcSrcRequest.getLength())
                    .fileSize(vcSrcRequest.getSize())
                    .extension(vcSrcRequest.getExtension()).build();
            
            log.info("[vcService] Save src 객체 생성  : {}", src); //SRC 객체 생성 확인
            VcSrcFile save = vcSrcFileRepository.save(src);// SRC 객체 저장
            log.info("[vcService] save 확인 : {} ", save);
            srcUrl.add(VcUrlResponse.builder()//response 객체 생성
                    .seq(save.getSrcSeq())
                    .url(save.getFileUrl())
                    .build());
        }
        return srcUrl;
    }

    /**
     * VC Trg 파일 저장
     * @param vcAudioRequest
     * @return VcUrlResponse
     */
    @Override
    public VcUrlResponse trgSave(@Valid @NotNull VcAudioRequest vcAudioRequest) {
        //프로젝트 조회, 객체 생성후 저장
        Vc vc = projectFind(vcAudioRequest.getSeq());
        //프로젝트 조회한 값과 입력한 값을 저장하기 위한 TRG 객체 생성
        VcTrgFile trg = VcTrgFile.builder()
                .vc(vc)
                .fileName(vcAudioRequest.getName())
                .fileUrl(vcAudioRequest.getFileUrl())
                .fileLength(vcAudioRequest.getLength())
                .fileSize(vcAudioRequest.getSize())
                .extension(vcAudioRequest.getExtension()).build();
        log.info("[vcService] Save trg 생성  : {}", trg); //TRG 객체 생성 확인
        VcTrgFile save = vcTrgFileRepository.save(trg);// TRG 객체 저장

        return VcUrlResponse.builder()//response 객체 생성
                .seq(save.getTrgSeq())
                .url(save.getFileUrl())
                .build();
    }

    /**
     * Vc Result 파일 저장 (vc 생성 파일)
     * @param vcAudioRequest
     * @return VcUrlResponse
     */
    @Override
    @Transactional
    public VcUrlResponse resultSave(@Valid @NotNull VcAudioRequest vcAudioRequest) {
        //SRCFile 조회
        VcSrcFile srcFile = vcSrcFileFind(vcAudioRequest.getSeq());
        log.info("[vcService] ResultSave srcFile find : {} ", srcFile);// SRC 확인

        //프로젝트 조회한 값과 SRC 조회한 값, 입력한 값을 저장하기 위한 ResultFile 객체 생성
        VcResultFile result = VcResultFile.builder()
                .srcSeq(srcFile)
                .fileName(vcAudioRequest.getName())
                .fileUrl(vcAudioRequest.getFileUrl())
                .fileLength(vcAudioRequest.getLength())
                .fileSize(vcAudioRequest.getSize())
                .extension(vcAudioRequest.getExtension()).build();
        log.info("[vcService] Save result 생성 : {}", result); // Result 객체 생성 확인
        VcResultFile save = vcResultFileRepository.save(result);// result 객체 저장

        return VcUrlResponse.builder()//response 객체 생성
                .seq(save.getResSeq())
                .url(save.getFileUrl())
                .build();
    }

    @Transactional
    public List<VcUrlResponse> resultSave(@Valid @NotNull List<VcAudioRequest> vcAudioRequests) {
        List<VcUrlResponse> resultFiles = new ArrayList<>();
        for (VcAudioRequest vcAudioRequest : vcAudioRequests) {
            //SRCFile 조회
            VcSrcFile srcFile = vcSrcFileFind(vcAudioRequest.getSeq());
            log.info("[vcService] ResultSave srcFile find : {} ", srcFile);// SRC 확인

            //프로젝트 조회한 값과 SRC 조회한 값, 입력한 값을 저장하기 위한 ResultFile 객체 생성
            VcResultFile result = VcResultFile.builder()
                    .srcSeq(srcFile)
                    .fileName(vcAudioRequest.getName())
                    .fileUrl(vcAudioRequest.getFileUrl())
                    .fileLength(vcAudioRequest.getLength())
                    .fileSize(vcAudioRequest.getSize())
                    .extension(vcAudioRequest.getExtension()).build();
            log.info("[vcService] Save result 생성 : {}", result); // Result 객체 생성 확인
            VcResultFile save = vcResultFileRepository.save(result);// result 객체 저장

            resultFiles.add(VcUrlResponse.builder()//response 객체 생성
                    .seq(save.getResSeq())
                    .url(save.getFileUrl())
                    .build());
        }
        return resultFiles;
    }


    /**
     * Text 저장 기능
     * @param vcTextRequest
     * @return VcTextResponse
     */
    @Override
    @Transactional
    public VcTextResponse textSave(@Valid @NotNull VcTextRequest vcTextRequest) {
        //SRC 조회
        VcSrcFile srcFile = vcSrcFileFind(vcTextRequest.getSeq());
        log.info("[vcService] TextSave srcFile find : {} ", srcFile);//SRC 확인

        //SRC 조회한 값과 프로젝트 조회한 값, 입력 값 저장하기 위한 TextFile 객체 생성
        VcText text = VcText.builder()
                .srcSeq(srcFile)
                .comment(vcTextRequest.getText())
                .length(String.valueOf(vcTextRequest.getText().length()))
                .build();
        log.info("[vcService] Save text 생성 : {}", text);//Text 객체 생성 값 확인

        VcText save = vcTextRepository.save(text);//Text 객체 저장

        return VcTextResponse.builder()//response 객체 생성
                .seq(save.getVtSeq())
                .text(save.getComment())
                .build();
    }

    /**
     * 리스트로 텍스트 저장
     * @param vcTextRequests
     * @return
     */
    @Override
    @Transactional
    public List<VcTextResponse> textSave(@Valid @NotNull List<VcTextRequest> vcTextRequests) {
        List<VcTextResponse> textReturn = new ArrayList<>();
        for (VcTextRequest vcTextRequest : vcTextRequests) {
            //SRC 조회
            VcSrcFile srcFile = vcSrcFileFind(vcTextRequest.getSeq());

            log.info("[vcService] TextSave srcFile find : {} ", srcFile);//SRC 확인

            //SRC 조회한 값과 프로젝트 조회한 값, 입력 값 저장하기 위한 TextFile 객체 생성
            VcText text = VcText.builder()
                    .srcSeq(srcFile)
                    .comment(vcTextRequest.getText())
                    .length(String.valueOf(vcTextRequest.getText().length()))
                    .build();
            log.info("[vcService] Save text 생성 : {}", text);//Text 객체 생성 값 확인

            VcText save = vcTextRepository.save(text);//Text 객체 저장

            textReturn.add(VcTextResponse.builder()//response 객체 생성
                    .seq(save.getVtSeq())
                    .text(save.getComment())
                    .build());
        }
        return textReturn;
    }


    /**
     * 프로젝트 VC 탭 조회 기능
     * @param projectSeq
     * @return List<VcResponse>
     */
    @Override
    @Transactional
    public List<VcResponse> getVcResponse(@Valid @NotNull Long projectSeq) {
        //프로젝트 seq 조회한 값
        List<VcSrcFile> vcSrcFileList = vcSrcFileRepository.findByVcProjectSeq(projectSeq);
        log.info("[vcService] GetVcResponse vcSrcFileList find : {} ", vcSrcFileList);
        //src, result, text 값 저장하기 위한 배열 생성
        List<VcResponse> vcResponseList = new ArrayList<>();

        for (VcSrcFile vcSrcFile : vcSrcFileList) {
            //src요청 값 입력
            VcSrcsRequest srcAudio = VcSrcsRequest.builder()
                    .seq(vcSrcFile.getSrcSeq())
                    .rowOrder(vcSrcFile.getRowOrder())
                    .name(vcSrcFile.getFileName())
                    .fileUrl(vcSrcFile.getFileUrl())
                    .build();
            log.info("[vcService] GetVcResponse srcAudio  : {} ", srcAudio);
            // SRC 로 제일 최근에 저장한 Result 조회 값이 없을 경우 null 출력
            VcResultFile vcResultFile = vcResultFileRepository.findFirstBySrcSeq_SrcSeqOrderBySrcSeqDesc(vcSrcFile.getSrcSeq());

            log.info("[vcService] GetVcResponse vcResultFile  : {} ", vcResultFile);
            //result 요청에 값 입력
            VcResultsRequest resultAudio = null;
            if(vcResultFile != null) {
                resultAudio = VcResultsRequest.builder()
                        .seq(vcResultFile.getResSeq()) //여기서 오류
                        .name(vcResultFile.getFileName())
                        .fileUrl(vcResultFile.getFileUrl())
                        .build();
            }


            log.info("[vcService] GetVcResponse resultAudio : {} ", resultAudio);
            //제일 최근에 저장한 텍스트 불러오기
            VcText vcText = vcTextRepository.findFirstBySrcSeq_SrcSeqOrderBySrcSeqDesc(vcSrcFile.getSrcSeq());
            log.info("[vcService] GetVcText find vcText : {} ", vcText);
            //text 요청에 값 입력
            VcTextRequest text = null;
            if(vcText != null) {
                text = VcTextRequest.builder()
                        .seq(vcText.getVtSeq())
                        .text(vcText.getComment())
                        .build();
            }

            log.info("[vcService] GetVcText text : {} ", text);

            // VcResponse 객체 생성 후 리스트에 추가
            VcResponse vcResponse = new VcResponse(srcAudio, resultAudio, text);
            vcResponseList.add(vcResponse);
        }
        log.info("[vcService] GetVcResponseList find : {} ", vcResponseList);
        return vcResponseList;
    }

    /**
     * VC SRC 파일 조회 기능
     * @param seq
     * @return VcUrlResponse
     */

    @Override
    public VcUrlResponse getSrcFile(@Valid @NotNull Long seq) {
        //SRC seq 로 SRC 값 조회
        VcSrcFile srcFile = vcSrcFileFind(seq);
        log.info("[vcService] getSrcFile VcSRcFile find : {} ", srcFile);//SRC 값 확인
        //S3 SRC URL 값 출력
        return VcUrlResponse.builder()
                .seq(srcFile.getSrcSeq())
                .url(srcFile.getFileUrl())
                .build();
    }

    /**
     * VC Result 파일 조회 기능
     * @param seq
     * @return VcUrlResponse
     */
    @Override
    public VcUrlResponse getResultFile(@Valid @NotNull Long seq) {
        //TRG seq 로 TRG 값 조회
        VcResultFile resultFile = vcResultFind(seq);
        log.info("[vcService] getResultFile ResultFile find : {} ", resultFile);//TRG 값 확인
        //S3 TRG URL 값 출력
        return VcUrlResponse.builder()
                .seq(resultFile.getResSeq())
                .url(resultFile.getFileUrl())
                .build();
    }

    /**
     * 텍스트 수정 기능
     * @param seq
     * @param text
     */
    @Override
    public VcTextResponse updateText(@Valid @NotNull Long seq, @Valid @NotNull String text) {
        //Text seq 로 Text 값 조회 검증
        VcText vcText = vcTextFind(seq);
        log.info("[vcService] updateText VcText find : {} ", vcText); //Text 값 확인
        //변경할 값과 seq 값 변경 객체 생성
        VcText updateText = vcText.toBuilder()
                .vtSeq(vcText.getVtSeq())
                .comment(text)
                .build();
        log.info("[vcService] updateText updateText find : {} ", updateText); //변경 객체 값 확인
        VcText save = vcTextRepository.save(updateText);//텍스트 값 변경

        return VcTextResponse.builder()
                .seq(save.getVtSeq())
                .text(save.getComment())
                .build();
    }

    /**
     * 행 수정 기능
     * @param seq
     * @param rowOrder
     */
    @Override
    @Transactional
    public VcRowResponse updateRowOrder(@Valid @NotNull Long seq, @Valid @NotNull int rowOrder) {
        //SRC seq 로 SRC 값 조회 검증
        VcSrcFile vcSrcFile = vcSrcFileFind(seq);
        log.info("[vcService] updateRowOrder vcSrcFile find : {} ", vcSrcFile);//SRC 값 확인
        //변경할 행순서 값과 SRC seq 값 변경 객체 생성
        VcSrcFile updateSrcFile = vcSrcFile.toBuilder()
                .srcSeq(vcSrcFile.getSrcSeq())
                .rowOrder(rowOrder)
                .build();
        log.info("[vcService] updateRowOrder updateSrcFile find : {} ", updateSrcFile);// 변경 객체 확인
        VcSrcFile save = vcSrcFileRepository.save(updateSrcFile);//행순서 변경
        return VcRowResponse.builder()
                .seq(save.getSrcSeq())
                .rowOrder(save.getRowOrder())
                .build();
    }

    /**
     * 행 순서 변경 리스트로
     * @param row
     * @return
     */
    @Override
    public List<VcRowResponse> updateRowOrder(List<VcRowRequest> row) {
        List<VcRowResponse> vcRowResponseList = new ArrayList<>();
        for (int i = 0; i < row.size(); i++) {
            //SRC seq 로 SRC 값 조회 검증
            VcSrcFile vcSrcFile = vcSrcFileFind(row.get(i).getSeq());
            //변경할 행순서 값과 SRC seq 값 변경 객체 생성
            VcSrcFile updateSrcFile = vcSrcFile.toBuilder()
                    .srcSeq(row.get(i).getSeq())
                    .rowOrder(row.get(i).getRowOrder())
                    .build();
            VcSrcFile save = vcSrcFileRepository.save(updateSrcFile);//행순서 변경
            vcRowResponseList.add(VcRowResponse.builder()
                    .seq(save.getSrcSeq())
                    .rowOrder(save.getRowOrder())
                    .build());
        }
        return vcRowResponseList;
    }

    /**
     * SRC 행 삭제 하는 기능(수정)
     * @param seq
     */
    @Override
    @Transactional
    public VcActivateResponse deleteSrcFile(@Valid @NotNull Long seq) {
        //SRC seq 로 SRC 값 조회 검증
        VcSrcFile vcSrcFile = vcSrcFileFind(seq);
        //활성화 상태 N로 변경
        VcSrcFile deleteSrcFile = vcSrcFile.toBuilder()
                .srcSeq(vcSrcFile.getSrcSeq())
                .activate('N')
                .build();
        log.info("[vcService] deleteSrcFile vcSrcFile find : {} ", deleteSrcFile);//변경 확인
        VcSrcFile save = vcSrcFileRepository.save(deleteSrcFile);//활성화상태 변경
        return VcActivateResponse.builder()
                .seq(save.getSrcSeq())
                .activate(save.getActivate())
                .build();
    }

    /**
     * 삭제 리스트로 변경
     * @param seqs
     * @return
     */
    @Override
    @Transactional
    public List<VcActivateResponse> deleteSrcFile(@Valid @NotNull List<Long> seqs) {
        List<VcActivateResponse> vcActivateResponseList = new ArrayList<>();
        for (Long seq : seqs) {
            //SRC seq 로 SRC 값 조회 검증
            VcSrcFile vcSrcFile = vcSrcFileFind(seq);
            //활성화 상태 N로 변경
            VcSrcFile deleteSrcFile = vcSrcFile.toBuilder()
                    .srcSeq(vcSrcFile.getSrcSeq())
                    .activate('N')
                    .build();
            log.info("[vcService] deleteSrcFile vcSrcFile find : {} ", deleteSrcFile);//변경 확인
            VcSrcFile save = vcSrcFileRepository.save(deleteSrcFile);//활성화상태 변경
            vcActivateResponseList.add(VcActivateResponse.builder()
                    .seq(save.getSrcSeq())
                    .activate(save.getActivate())
                    .build());
        }
        return vcActivateResponseList;
    }

    //VcSrcFile 찾는 메서드
    private VcSrcFile vcSrcFileFind(Long seq){
        return vcSrcFileRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("Src file not found"));
    }
    //Project 찾고 vc생성 저장하는 메서드
    private Vc projectFind(Long seq){
        Project project = projectRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        log.info("[vcService] TextSave Project find : {} ", project);//프로젝트 확인
        //VC를 찾았는데 없으면 Save 시키고 값을 주고 있으면 그값을 가지고 오는
        Vc vcSave = vcRepository.findById(project.getProSeq())
                .orElseGet(() -> vcRepository.save(
                        Vc.builder()
                                .proSeq(project)
                                .build()
                ));
        log.info("[vcService] TextSave Project find : {} ", vcSave);
        return vcSave;
    }
    //VcResultFile 찾는 메서드
    private VcResultFile vcResultFind(Long seq){
        return vcResultFileRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("ResultFile not found"));
    }
    //VcText 찾는 메서드
    private VcText vcTextFind(Long seq){
        return vcTextRepository.findById(seq)
                .orElseThrow(() -> new IllegalArgumentException("Text not found"));
    }

    /**
     * VCSrcRequest 객체 생성
     * @param audioFileInfos
     * @param upload
     * @param proSeq
     * @return
     */
    @Override
    public List<VcSrcRequest> vcSrcRequestBuilder(List<AudioFileInfo> audioFileInfos,
                                                          List<String> upload,
                                                          Long proSeq){
        List<VcSrcRequest> requests = new ArrayList<>();
        for (int i = 0; i < audioFileInfos.size(); i++) {
            AudioFileInfo info = audioFileInfos.get(i);
            String fileUrl = upload.get(i);
            VcSrcRequest request = VcSrcRequest.builder()
                    .seq(proSeq)
                    .rowOrder(1)
                    .name(info.getName())
                    .fileUrl(fileUrl)
                    .length(info.getLength())
                    .size(info.getSize())
                    .extension(info.getExtension())
                    .build();
            requests.add(request);
        }
        return requests;
    }

    /**
     * VcAudioRequest 객체 생성
     * @param proSeq
     * @param info
     * @param url
     * @return
     */
    @Override
    public VcAudioRequest audioRequestBuilder(Long proSeq, AudioFileInfo info, String url) {
        return VcAudioRequest.builder()
                .seq(proSeq)
                .name(info.getName())
                .fileUrl(url)
                .length(info.getLength())
                .size(info.getSize())
                .extension(info.getExtension())
                .build();
    }

    /**
     * VCAudioRequest 객체 생성
     * @param vcSrcUrlRequest
     * @param info
     * @param url
     * @return
     */
    @Override
    public List<VcAudioRequest> audioRequestBuilder(List<VcSrcUrlRequest> vcSrcUrlRequest, List<AudioFileInfo> info, List<String> url) {
        List<VcAudioRequest> result = new ArrayList<>();
        for (int i = 0; i < vcSrcUrlRequest.size(); i++) {
            VcAudioRequest vc = VcAudioRequest.builder()
                    .seq(vcSrcUrlRequest.get(i).getSeq())
                    .name(info.get(i).getName())
                    .fileUrl(url.get(i))
                    .length(info.get(i).getLength())
                    .size(info.get(i).getSize())
                    .extension(info.get(i).getExtension())
                    .build();
            result.add(vc);
        }
        return result;
    }

    /**
     * VcTextRequest 객체 생성
     * @param srcSeq
     * @param text
     * @return
     */
    @Override
    public List<VcTextRequest> vcTextResponses(List<Long> srcSeq, List<String> text) {
        List<VcTextRequest> vcTextResponses = new ArrayList<>();
        for (int i = 0; i < srcSeq.size(); i++) {
            VcTextRequest textRequest = VcTextRequest.builder()//Text 객체 생성
                    .seq(srcSeq.get(i))
                    .text(text.get(i))
                    .build();
            vcTextResponses.add(textRequest);
        }
        return vcTextResponses;
    }

    /**
     * Srcseq로 url을 찾는 메서드
     * @param srcSeq
     * @return
     */
    @Override
    public List<VcSrcUrlRequest> vcSrcUrlRequests(List<Long> srcSeq){
        List<VcSrcUrlRequest> vcSrcUrlRequest = new ArrayList<>();
        for (int i = 0; i < srcSeq.size(); i++) {
            //Seq로 VcSrcFile을 찾는다.
            VcSrcFile vcSrcFile = vcSrcFileRepository.findById(srcSeq.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("srcSeq not found"));
            //Url 과 Seq를 넣는다.
            VcSrcUrlRequest request = VcSrcUrlRequest.builder()
                    .url(vcSrcFile.getFileUrl())
                    .seq(vcSrcFile.getSrcSeq())
                    .build();
            //배열에 넣는다.
            vcSrcUrlRequest.add(request);
        }
        //배열로 반환한다.
        return vcSrcUrlRequest;
    }
}