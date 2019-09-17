import * as React from 'react';
import {style} from 'typestyle';
import {MeterGridContainer} from '../../containers/MeterGridContainer';
import {Maybe} from '../../helpers/Maybe';
import {translate} from '../../services/translationService';
import {TotalElements} from '../../state/domain-models-paginated/paginatedDomainModels';
import {ErrorResponse, HasContent, OnClick} from '../../types/Types';
import {OwnProps} from '../../usecases/meter/meterModels';
import {MeterListActionsDropdown, Props} from '../actions-dropdown/MeterListActionsDropdown';
import {withContent} from '../hoc/withContent';
import {Column} from '../layouts/column/Column';
import {NumItems} from '../lists/NumItems';
import {Retry} from '../retry/Retry';

const className = style({
  position: 'relative',
  $nest: {
    '.SelectionResultActionDropdown': {
      position: 'absolute',
      top: -32,
      right: 18,
    },
  },
});

export interface StateToProps extends HasContent, TotalElements {
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
  totalElements,
}: StateToProps & DispatchToProps & OwnProps) => (
  <Retry error={error} clearError={clearMetersErrorOnPage}>
    <Column>
      <Column className={className}>
        <MeterListActionsDropdownWrapper
          addAllToReport={addMetersOnPageToReport}
          syncMeters={syncMetersOnPage}
          hasContent={hasContent}
        />
        <MeterGridContainer paddingBottom={paddingBottom}/>
      </Column>
      <NumItems hasContent={hasContent} label={translate('total meters')} totalElements={totalElements}/>
    </Column>
  </Retry>
);
