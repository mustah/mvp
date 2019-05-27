import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DispatchToProps} from '../../../components/tabs/components/MainContentTabs';
import {RootState} from '../../../reducers/rootReducer';
import {changeTabMeter} from '../../../state/ui/tabs/tabsActions';
import {SelectedTab} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {MeterTabs} from '../components/MeterTabs';

const mapStateToProps = ({ui}: RootState): SelectedTab => ({
  selectedTab: getSelectedTab(ui),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabMeter,
}, dispatch);

export const MeterTabsContainer = connect(mapStateToProps, mapDispatchToProps)(MeterTabs);
