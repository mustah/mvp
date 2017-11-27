import {SelectedTabs, TabModel, tabType} from '../../../components/tabs/models/TabsModel';

export const getSelectedTab = (state: SelectedTabs): tabType => state.selectedTab;
export const getTabs = (state: SelectedTabs): TabModel => state.tabs;
