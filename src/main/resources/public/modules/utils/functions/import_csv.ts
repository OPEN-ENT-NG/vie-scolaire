import {notify, $} from "entcore";
import {Attachment} from "../../viescolaire/models/common/Attachement";
import {safeApply} from "./safeApply";
import http from 'axios';

export let  postAttachments = (fichiers, $scope) => {

    $scope.loadingAttachments = [];
    $scope.newAttachments = fichiers;

    const promises: Promise<any>[] = [];
    for (let i = 0; i < $scope.newAttachments.length; i++) {
        const targetAttachment = $scope.newAttachments[i];
        const attachmentObj = new Attachment(targetAttachment);
        $scope.loadingAttachments.push(attachmentObj);

        const formData = new FormData();
        formData.append('file', attachmentObj.file);
        if($scope.import.classe !== undefined && $scope.import.periode != undefined) {
            let idPeriode = $scope.import.periode.id_type;
            let idClasse = $scope.import.classe.id;
            let url = `/viescolaire/import/evenements/${idClasse}/${idPeriode}`;
            const promise = http.post(url, formData, {
                onUploadProgress: (e: ProgressEvent) => {
                    if (e.lengthComputable) {
                        let percentage = Math.round((e.loaded * 100) / e.total);
                        console.log(percentage);
                        attachmentObj.progress.completion = percentage;
                        $scope.$apply();
                    }
                }
            })
                .then(response => {
                    $scope.loadingAttachments.splice($scope.loadingAttachments.indexOf(attachmentObj), 1);

                    attachmentObj._id = response.data._id;
                    attachmentObj.filename = attachmentObj.file.name;
                    attachmentObj.size = attachmentObj.file.size;
                    attachmentObj.contentType = attachmentObj.file.type;
                    $scope.attachments.push(attachmentObj);

                    // On reset le file input
                    $('#input-attachment-declaration').val('');

                    safeApply($scope);
                })
                .catch(e => {
                    $scope.loadingAttachments.splice($scope.loadingAttachments.indexOf(attachmentObj), 1);
                    notify.error(e.response.data.error);

                    safeApply($scope);
                });

            promises.push(promise);
        }
        else {
            notify.info('please.set.periode.and.classe');
        }
    }

    return Promise.all(promises);
};

export let deleteAttachment = (idAttachment, $scope) => {
    $scope.attachments = $scope.attachments.filter(obj => obj._id !== idAttachment);
};
