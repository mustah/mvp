import {SelectedTabs, TabModel, tabType} from '../../../usecases/common/components/tabs/models/TabsModel';

export const getSelectedTab = (state: SelectedTabs): tabType => state.selectedTab;
export const getTabs = (state: SelectedTabs): TabModel => state.tabs;
