import {Grid, GridCellProps, GridColumn} from '@progress/kendo-react-grid';
import {toArray} from 'lodash';
import * as React from 'react';
import {borderRadius, gridStyle} from '../../../app/themes';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {componentOrNothing} from '../../../components/hoc/hocs';
import {IconIndicator} from '../../../components/icons/IconIndicator';
import {Column} from '../../../components/layouts/column/Column';
import {RowRight} from '../../../components/layouts/row/Row';
import {isDefined} from '../../../helpers/commonUtils';
import {orUnknown} from '../../../helpers/translations';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {Normalized} from '../../../state/domain-models/domainModels';
import {Medium} from '../../../state/ui/graph/measurement/measurementModels';
import {Clickable, OnClick, OnClickWithId, Titled, uuid} from '../../../types/Types';
import {LegendItem} from '../reportModels';
import './Legend.scss';

interface IsReportPage {
  isReportPage: boolean;
}

export interface LegendProps extends IsReportPage {
  removeSelectedListItems: OnClick;
  deleteItem: OnClickWithId;
  hideAllLines: OnClick;
  hiddenLines: uuid[];
  legendItems: Normalized<LegendItem>;
  toggleLine: OnClick;
}

const iconIndicatorStyle: React.CSSProperties = {
  display: 'table',
  width: 24,
  height: 24,
};

const legendGridStyle: React.CSSProperties = {
  ...gridStyle,
  borderTopLeftRadius: borderRadius,
  borderTopRightRadius: borderRadius,
  marginBottom: 24,
  width: 336,
  maxHeight: 0.75 * window.innerHeight
};

const renderFacility = ({dataItem: {facility, city, address}}: GridCellProps) =>
  facility
    ? (
      <td className="left-most first-uppercase" title={`${orUnknown(city)}, ${orUnknown(address)}`}>
        {orUnknown(facility)}
      </td>
    )
    : <td>-</td>;

const renderMediumCell = (medium: Medium) =>
  Array.isArray(medium)
    ? medium.map((singleMedium: Medium, index) => (
      <IconIndicator
        key={`${singleMedium}-${index}`}
        medium={singleMedium}
        style={iconIndicatorStyle}
      />
    ))
    : <IconIndicator medium={medium} style={iconIndicatorStyle}/>;

const renderMedium = ({dataItem: {medium}}: GridCellProps) =>
  <td>{renderMediumCell(medium)}</td>;

type ButtonProps = Partial<IsReportPage> & Clickable & Titled;

const DeleteButton = ({onClick, title}: ButtonProps) =>
  <RowRight title={title}><ButtonDelete onClick={onClick}/></RowRight>;

const isReportPagePredicate = ({isReportPage}: ButtonProps): boolean => isReportPage !== undefined && isReportPage;

const ReportDeleteButton = componentOrNothing<ButtonProps>(isReportPagePredicate)(DeleteButton);

export const Legend = ({
  removeSelectedListItems,
  hideAllLines,
  hiddenLines,
  isReportPage,
  legendItems: {entities: {lines}},
  toggleLine,
  deleteItem
}: LegendProps) => {
  const renderIconButtonsHeader = () => (
    <RowRight>
      <RowRight title={firstUpperTranslated('hide all')}>
        <ButtonVisibility onClick={hideAllLines}/>
      </RowRight>
      <ReportDeleteButton
        onClick={removeSelectedListItems}
        title={firstUpperTranslated('remove all')}
        isReportPage={isReportPage}
      />
    </RowRight>
  );

  const renderIconButtons = ({dataItem: {id}}: GridCellProps) => {
    const checked = isDefined(hiddenLines.find((it) => it === id));
    const onDeleteItem = () => deleteItem(id);
    const onToggleItem = () => toggleLine(id);
    return (
      <td className="icons">
        <RowRight>
          <RowRight>
            <ButtonVisibility key={`checked-${id}-${checked}`} onClick={onToggleItem} checked={checked}/>
          </RowRight>
          <ReportDeleteButton onClick={onDeleteItem} isReportPage={isReportPage}/>
        </RowRight>
      </td>
    );
  };

  const data = React.useMemo<LegendItem[]>(() => toArray(lines), [lines]);

  return (
    <Column className="Legend">
      <Grid data={data} style={legendGridStyle}>
        <GridColumn
          field="facility"
          cell={renderFacility}
          title={translate('facility')}
          headerClassName="left-most"
          width={140}
        />
        <GridColumn
          field="medium"
          cell={renderMedium}
          title={translate('medium')}
          width={90}
        />
        <GridColumn
          headerCell={renderIconButtonsHeader}
          headerClassName="Link"
          cell={renderIconButtons}
          width={90}
        />
      </Grid>
    </Column>
  );
};
