import {notify, $, idiom as lang} from "entcore";
import {Attachment} from "../../viescolaire/models/common/Attachement";
import {safeApply} from "./safeApply";
import http from 'axios';

export let postAttachments = (fichiers, $scope) => {
    $scope.loadingAttachments = [];
    $scope.newAttachments = fichiers;
    for (let i = 0; i < fichiers.length; i++) {
        const targetAttachment = fichiers[i];
        const attachmentObj = new Attachment(targetAttachment);
        $scope.loadingAttachments.push(attachmentObj);
        $scope.attachments.push(attachmentObj);
    }
};
export let importAttachments = (fichiers, $scope) => {
    $scope.newAttachments = (fichiers !== undefined)? fichiers : [];
    const promises: Promise<any>[] = [];

    if($scope.import.classe !== undefined && $scope.import.periode != undefined) {
    for (let i = 0; i < $scope.newAttachments.length; i++) {
        const attachmentObj = $scope.newAttachments[i];

        const formData = new FormData();
        formData.append('file', attachmentObj.file);

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


                    // On reset le file input
                    $('#input-attachment-declaration').val('');
                    if (response.data.error) {
                        notify.error(lang.translate('import.csv.error'));
                    }
                    else {
                        if(!response.data.isValid) {
                            notify.error(lang.translate('import.csv.error.invalid'));
                        }
                        else{
                            let nbInsert = response.data.nbInsert;
                            let nbNotInsert = response.data.nbLines - nbInsert;
                            notify.success(lang.translate('import.csv.success')
                                +  nbInsert
                                + lang.translate('import.students')
                                + nbNotInsert
                                + lang.translate('import.user.not.insert')
                            );
                        }
                    }
                    safeApply($scope);
                })
                .catch(e => {
                    $scope.loadingAttachments.splice($scope.loadingAttachments.indexOf(attachmentObj), 1);
                    notify.error(lang.translate('import.csv.error'));

                    safeApply($scope);
                });

            promises.push(promise);
        }
    }
    else {
        notify.info('please.set.periode.and.classe');
    }
    return Promise.all(promises);
};

export let deleteAttachment = (filename, $scope) => {
    $scope.attachments = $scope.attachments.filter(obj => obj.filename !== filename);
};
