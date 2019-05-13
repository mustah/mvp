import * as React from 'react';
import {ThemeContext, withCssStyles} from '../../../../components/hoc/withThemeProvider';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {initialSelectionId, UserSelection} from '../../../../state/user-selection/userSelectionModels';
import {CallbackWith, OnClick, OnClickWithId} from '../../../../types/Types';
import {InlineEditInput} from './InlineEditInput';

export interface StateToProps {
  selection: UserSelection;
}

export interface DispatchToProps {
  resetSelection: OnClick;
  resetToSavedSelection: OnClickWithId;
  saveSelection: CallbackWith<UserSelection>;
  updateSelection: CallbackWith<UserSelection>;
}

type Props = StateToProps & DispatchToProps & ThemeContext;

export const SelectionMenu = withCssStyles((props: Props) => (
  <RowMiddle>
    <InlineEditInput
      key={`${props.selection.id}-${props.selection.id === initialSelectionId ? '' : props.selection.isChanged}`}
      isChanged={props.selection.isChanged}
      {...props}
    />
  </RowMiddle>
));
