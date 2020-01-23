import MementoViewModel from './memento';

declare let window: any;

export const MementoSniplet = {
    title: 'viescolaire.memento.title',
    public: false,
    controller: {
        init: function () {
            console.log('memento sniplet controller');
            this.vm = MementoViewModel;
            this.vm.setApplier(this);

            window.memento = {
                load: MementoViewModel.loadMemento,
                close: MementoViewModel.closeMemento,
            };

            this.vm.init();
        }
    }
};