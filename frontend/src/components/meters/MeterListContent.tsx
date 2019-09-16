import * as React from 'react';
import {style} from 'typestyle';
import {colors} from '../../app/colors';
import {dividerBorder} from '../../app/themes';
import {MeterGridContainer} from '../../containers/MeterGridContainer';
import {Maybe} from '../../helpers/Maybe';
import {translate} from '../../services/translationService';
import {ErrorResponse, HasContent, OnClick} from '../../types/Types';
import {OwnProps} from '../../usecases/meter/meterModels';
import {MeterListActionsDropdown, Props} from '../actions-dropdown/MeterListActionsDropdown';
import {withContent} from '../hoc/withContent';
import {withEmptyContentComponent} from '../hoc/withEmptyContent';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Retry} from '../retry/Retry';
import {Bold, Normal} from '../texts/Texts';

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

export interface StateToProps extends HasContent {
  error: Maybe<ErrorResponse>;
  totalElements: number;
}

export interface DispatchToProps {
  addMetersOnPageToReport: OnClick;
  clearMetersErrorOnPage: OnClick;
  syncMetersOnPage: OnClick;
}

const MeterListActionsDropdownWrapper = withContent<Props>(MeterListActionsDropdown);

interface NumMetersProps {
  totalElements: number;
}

const PlaceholderWhileLoading = () => <Row style={{height: 50}}/>;

const NumMeters = withEmptyContentComponent(({totalElements}: NumMetersProps) => (
  <Row style={{padding: 16, marginTop: 1, borderTop: dividerBorder}}>
    <Normal style={{color: colors.info}} className="uppercase">{`${translate('total meters')}:`}</Normal>
    <Bold style={{marginLeft: 8}}>{totalElements}</Bold>
  </Row>
), PlaceholderWhileLoading);

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
      <NumMeters hasContent={hasContent} totalElements={totalElements}/>
    </Column>
  </Retry>
);
