import {AutoCompleteUtils} from './auto-complete';
import {ISearchService} from '../../services';
import {IGroup} from '../../models/common/Group';

/**
 * ⚠ This class is used for the directive async-autocomplete
 * use it for group only (groups/classes)
 */

export class GroupsSearch extends AutoCompleteUtils {

    private groups: Array<IGroup>;
    private selectedGroups: Array<{}>;

    public group: string;

    constructor(structureId: string, searchService: ISearchService) {
        super(structureId, searchService);
    }

    public getGroups() {
        return this.groups;
    }

    public getSelectedGroups() {
        return this.selectedGroups ? this.selectedGroups : [];
    }

    public setSelectedGroups(selectedGroups: Array<{}>) {
        this.selectedGroups = selectedGroups;
    }

    public removeSelectedGroups(groupItem: IGroup) {
        this.selectedGroups.splice(this.selectedGroups.indexOf(groupItem), 1);
    }

    public resetGroups() {
        this.groups = [];
    }

    public resetSelectedGroups() {
        this.selectedGroups = [];
    }

    public selectGroups(valueInput: string, groupItem: IGroup) {
        if (!this.selectedGroups) this.selectedGroups = [];
        if (this.selectedGroups.find(group => group['id'] === groupItem.id) === undefined) {
            this.selectedGroups.push(groupItem);
        }
    }

    public selectGroup(valueInput: string, groupItem: IGroup) {
        this.selectedGroups = [];
        this.selectedGroups.push(groupItem);
    }

    public async searchGroups(valueInput: string) {
        try {
            this.groups = await this.searchService.searchGroups(this.structureId, valueInput);
            this.groups.map((group: IGroup) => group.toString = () => group.name);
        } catch (err) {
            this.groups = [];
            throw err;
        }
    }
}