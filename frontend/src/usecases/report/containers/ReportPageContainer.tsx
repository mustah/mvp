import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {fetchSelectionTree} from '../../../state/selection-tree/selectionTreeApiActions';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {EncodedUriParameters, Fetch} from '../../../types/Types';
import {ReportContainer} from './ReportContainer';

interface StateToProps {
  isFetching: boolean;
  parameters: EncodedUriParameters;
}

interface DispatchToProps {
  fetchSelectionTree: Fetch;
}

const Component = ({fetchSelectionTree, parameters}: DispatchToProps & StateToProps) => {
  React.useEffect(() => {
    fetchSelectionTree(parameters);
  }, [parameters]);
  return <ReportContainer/>;
};

const mapStateToProps = ({userSelection: {userSelection}, selectionTree}: RootState): StateToProps =>
  ({
    isFetching: selectionTree.isFetching,
    parameters: getMeterParameters({userSelection}),
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchSelectionTree,
}, dispatch);

export const ReportPageContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Component);
