import {connect} from 'react-redux';
import {RootState} from '../../../../reducers/rootReducer';
import {MeterDetails} from '../../../../state/domain-models/meter-details/meterDetailsModels';
import {ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {CollectionContent} from '../components/CollectionContent';

interface StateToProps {
  view: ToolbarView;
}

interface OwnProps {
  meter: MeterDetails;
}

const mapStateToProps = ({ui: {toolbar: {meterCollection: {view}}}}: RootState): StateToProps => ({view});

export const MeterCollectionContentContainer =
  connect<StateToProps, null, OwnProps>(mapStateToProps)(CollectionContent);
