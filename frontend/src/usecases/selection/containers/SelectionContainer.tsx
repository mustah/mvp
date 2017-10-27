import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {RootState} from '../../../reducers/rootReducer';
import {PageContainer} from '../../common/containers/PageContainer';
import {SelectionContentBox} from '../components/SelectionContentBox';
import {SelectionParameter} from '../../../state/search/selection/selectionModels';
import {toggleSelection} from '../../../state/search/selection/selectionActions';
import {SelectionState} from '../../../state/search/selection/selectionReducer';
import {SelectionOptionsLoaderContainer} from './SelectionOptionsLoaderContainer';

export interface SelectionStateToProps {
  selection: SelectionState;
}

export interface SelectionDispatchToProps {
  toggleSearchOption: (searchParameters: SelectionParameter) => void;
}

type Props = SelectionStateToProps & SelectionDispatchToProps & InjectedAuthRouterProps;

const SelectionContainerComponent = (props: Props) =>
  (
    <PageContainer>
      <SelectionOptionsLoaderContainer>
        <SelectionContentBox
          selection={props.selection}
          toggleSearchOption={props.toggleSearchOption}
        />
      </SelectionOptionsLoaderContainer>
    </PageContainer>
  );

const mapStateToProps = ({selection}: RootState): SelectionStateToProps => {
  return {
    selection,
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  toggleSearchOption: toggleSelection,
}, dispatch);

export const SelectionContainer =
  connect<SelectionStateToProps, SelectionDispatchToProps, {}>
  (mapStateToProps, mapDispatchToProps)(SelectionContainerComponent);
