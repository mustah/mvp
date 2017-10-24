import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history3/redirect';
import {RootState} from '../../../reducers/index';
import {PageContainer} from '../../common/components/layouts/layout/PageLayout';
import {SelectionContentBox} from '../components/SelectionContentBox';
import {SelectionParameter} from '../models/selectionModels';
import {toggleSelection} from '../selectionActions';
import {SelectionState} from '../selectionReducer';
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
