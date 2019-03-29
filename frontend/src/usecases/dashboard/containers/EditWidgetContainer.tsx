import {connect} from 'react-redux';
import {RootState} from '../../../reducers/rootReducer';
import {NormalizedState} from '../../../state/domain-models/domainModels';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {EditWidget} from '../components/EditWidget';

export interface StateToProps {
  userSelections: NormalizedState<UserSelection>;
}

const mapStateToProps = ({domainModels: {userSelections}}: RootState): StateToProps => ({
  userSelections,
});

export const EditWidgetContainer = connect<StateToProps>(mapStateToProps)(EditWidget);
