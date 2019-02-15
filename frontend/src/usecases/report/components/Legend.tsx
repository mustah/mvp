import {Grid, GridCellProps, GridColumn} from '@progress/kendo-react-grid';
import * as React from 'react';
import {borderRadius, gridStyle} from '../../../app/themes';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {componentOrNothing} from '../../../components/hoc/hocs';
import {Column} from '../../../components/layouts/column/Column';
import {RowLeft, RowRight} from '../../../components/layouts/row/Row';
import {isDefined} from '../../../helpers/commonHelpers';
import {orUnknown} from '../../../helpers/translations';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {Medium, toMediumText} from '../../../state/ui/graph/measurement/measurementModels';
import {Clickable, Titled} from '../../../types/Types';
import {DispatchToProps, StateToProps} from '../containers/LegendContainer';
import './Legend.scss';

interface IsReportPage {
  isReportPage: boolean;
}

const legendGridStyle: React.CSSProperties = {
  ...gridStyle,
  borderTopLeftRadius: borderRadius,
  borderTopRightRadius: borderRadius,
  marginBottom: 24,
  width: 370,
  maxHeight: 0.75 * window.innerHeight
};

const renderLabel = ({dataItem: {label, city, address}}: GridCellProps) =>
  label
    ? (
      <td className="left-most first-uppercase" title={`${orUnknown(city)}, ${orUnknown(address)}`}>
        {orUnknown(label)}
      </td>
    )
    : <td>-</td>;

const renderMediumCell = (medium: Medium) =>
  Array.isArray(medium)
    ? medium.map((singleMedium: Medium, index) =>
      index > 1 && index < medium.length ? `${toMediumText(medium)}, ` : toMediumText(medium))
    : toMediumText(medium);

const renderMedium = ({dataItem: {medium}}: GridCellProps) =>
  <td>{renderMediumCell(medium)}</td>;

type ButtonProps = Partial<IsReportPage> & Clickable & Titled;

const DeleteButton = ({onClick, title}: ButtonProps) =>
  <RowRight title={title}><ButtonDelete onClick={onClick}/></RowRight>;

const isReportPagePredicate = ({isReportPage}: ButtonProps): boolean => isReportPage !== undefined && isReportPage;

const ReportDeleteButton = componentOrNothing<ButtonProps>(isReportPagePredicate)(DeleteButton);

export const Legend = ({
  deleteItem,
  hideAllLines,
  hiddenLines,
  isReportPage,
  legendItems,
  removeSelectedListItems,
  toggleLine
}: DispatchToProps & StateToProps) => {
  const renderIconButtonsHeader = () => (
    <RowLeft>
      <RowRight title={firstUpperTranslated('hide all')}>
        <ButtonVisibility onClick={hideAllLines}/>
      </RowRight>
      <ReportDeleteButton
        onClick={removeSelectedListItems}
        title={firstUpperTranslated('remove all')}
        isReportPage={isReportPage}
      />
    </RowLeft>
  );

  const renderIconButtons = ({dataItem: {id}}: GridCellProps) => {
    const checked = isDefined(hiddenLines.find((it) => it === id));
    const onDeleteItem = () => deleteItem(id);
    const onToggleItem = () => toggleLine(id);
    return (
      <td className="icons">
        <RowLeft>
          <RowRight>
            <ButtonVisibility key={`checked-${id}-${checked}`} onClick={onToggleItem} checked={checked}/>
          </RowRight>
          <ReportDeleteButton onClick={onDeleteItem} isReportPage={isReportPage}/>
        </RowLeft>
      </td>
    );
  };

  return (
    <Column className="Legend">
      <Grid data={legendItems} style={legendGridStyle}>
        <GridColumn
          cell={renderLabel}
          title={translate('facility')}
          headerClassName="left-most"
          width={144}
        />
        <GridColumn
          cell={renderMedium}
          title={translate('medium')}
          width={144}
        />
        <GridColumn
          headerCell={renderIconButtonsHeader}
          headerClassName="Link"
          cell={renderIconButtons}
          width={76}
        />
      </Grid>
    </Column>
  );
};
