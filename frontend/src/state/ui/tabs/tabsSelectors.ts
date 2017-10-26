import {SelectedTabs} from '../../../usecases/common/components/tabs/models/TabsModel';

export const getSelectedTab = (state: SelectedTabs) => state.selectedTab;
export const getTabs = (state: SelectedTabs) => state.tabs;
