import * as React from 'react';
import {style} from 'typestyle';
import {MeterGridContainer} from '../../containers/MeterGridContainer';
import {Maybe} from '../../helpers/Maybe';
import {ErrorResponse, HasContent, OnClick} from '../../types/Types';
import {OwnProps} from '../../usecases/meter/meterModels';
import {MeterListActionsDropdown, Props} from '../actions-dropdown/MeterListActionsDropdown';
import {withContent} from '../hoc/withContent';
import {Column} from '../layouts/column/Column';
import {Retry} from '../retry/Retry';

const className = style({
  position: 'relative',
  $nest: {
    '.SelectionResultActionDropdown': {
      position: 'absolute',
      top: -32,
      right: 8,
    },
  },
});

export interface StateToProps extends HasContent {
  error: Maybe<ErrorResponse>;
}

export interface DispatchToProps {
  addMetersOnPageToReport: OnClick;
  clearMetersErrorOnPage: OnClick;
  syncMetersOnPage: OnClick;
}

const MeterListActionsDropdownWrapper = withContent<Props>(MeterListActionsDropdown);

export const MeterListContent = ({
  addMetersOnPageToReport,
  clearMetersErrorOnPage,
  error,
  hasContent,
  paddingBottom,
  syncMetersOnPage,
}: StateToProps & DispatchToProps & OwnProps) => (
  <Retry error={error} clearError={clearMetersErrorOnPage}>
    <Column className={className}>
      <MeterListActionsDropdownWrapper
        addAllToReport={addMetersOnPageToReport}
        syncMeters={syncMetersOnPage}
        hasContent={hasContent}
      />
      <MeterGridContainer paddingBottom={paddingBottom}/>
    </Column>
  </Retry>
);
