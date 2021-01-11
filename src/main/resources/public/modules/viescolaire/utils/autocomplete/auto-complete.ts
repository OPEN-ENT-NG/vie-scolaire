import {ISearchService} from '../../services/SearchService';


export class AutoCompleteUtils {

    protected readonly structureId: string;

    protected searchService: ISearchService;

    constructor(structureId: string, searchService: ISearchService) {
        this.structureId = structureId;
        this.searchService = searchService;
    }

}
