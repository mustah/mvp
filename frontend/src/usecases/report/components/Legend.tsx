import {DataResult, process, State} from '@progress/kendo-data-query';
import {Grid, GridCellProps, GridColumn} from '@progress/kendo-react-grid';
import {findIndex} from 'lodash';
import * as React from 'react';
import {borderRadius, gridStyle} from '../../../app/themes';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {Column} from '../../../components/layouts/column/Column';
import {RowLeft, RowRight} from '../../../components/layouts/row/Row';
import {isDefined} from '../../../helpers/commonHelpers';
import {orUnknown} from '../../../helpers/translations';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {DispatchToProps, StateToProps} from '../containers/LegendContainer';
import './Legend.scss';
import {quantityWidth, renderColumns, rowRenderer} from '../helpers/legendGridHelper';
import {cellRender, headerCellRender} from '../helpers/measurementGridHelper';

const legendGridStyle: React.CSSProperties = {
  ...gridStyle,
  borderTopLeftRadius: borderRadius,
  borderTopRightRadius: borderRadius,
  marginBottom: 24,
  maxHeight: 0.75 * window.innerHeight
};

const renderLabelCell = ({dataItem: {label, city, address}}: GridCellProps) =>
  label
    ? (
      <td className="left-most first-uppercase" title={`${orUnknown(city)}, ${orUnknown(address)}`}>
        {orUnknown(label)}
      </td>
    )
    : <td>-</td>;

const updateDataResult = (gridState: LegendState, dataItem: any): DataResult => {
  const result: DataResult = gridState.result;
  const index = findIndex(result.data, {value: dataItem.value});
  const expanded = !dataItem.expanded;
  const lastIndex = dataItem.items.length - 1;
  const items = dataItem.items.map((it, index) => ({...it, expanded, last: index === lastIndex}));
  result.data[index] = {...dataItem, expanded, items};
  return result;
};

const state: State = {group: [{field: 'medium'}]};

interface LegendState {
  result: DataResult;
}

export const Legend = ({
  deleteItem,
  hideAllLines,
  hiddenLines,
  isAllLinesHidden,
  isReportPage,
  legendItems,
  removeSelectedListItems,
  toggleLine
}: DispatchToProps & StateToProps) => {
  const [gridState, setGridState] = React.useState<LegendState>({result: process(legendItems, state)});
  const [quantityGridColumns, columnQuantities] = React.useMemo(() => renderColumns(legendItems), [legendItems]);

  const renderIconButtonsHeaderCell = () => (
    <RowLeft>
      <RowRight title={firstUpperTranslated('hide all')}>
        <ButtonVisibility
          key={`check-all-${isAllLinesHidden}`}
          onClick={hideAllLines}
          checked={isAllLinesHidden}
        />
      </RowRight>
      <RowRight title={firstUpperTranslated('remove all')}>
        <ButtonDelete onClick={removeSelectedListItems}/>
      </RowRight>
    </RowLeft>
  );

  const renderIconButtonsCell = ({dataItem: {id}}: GridCellProps) => {
    const checked = isDefined(hiddenLines.find((it) => it === id));
    const onDeleteItem = () => deleteItem(id);
    const onToggleItem = () => toggleLine(id);
    return (
      <td className="icons">
        <RowLeft>
          <RowRight>
            <ButtonVisibility key={`checked-${id}-${checked}`} onClick={onToggleItem} checked={checked}/>
          </RowRight>
          <RowRight>
            <ButtonDelete key={`delete-item-${id}`} onClick={onDeleteItem}/>
          </RowRight>
        </RowLeft>
      </td>
    );
  };

  const facilityWidth = 138;
  const iconsWidth = 76;
  const width = facilityWidth + iconsWidth + (columnQuantities.length * quantityWidth) + 28;

  const gridContent: React.ReactNode[] = [
    (
      <GridColumn
        key="facility"
        cell={renderLabelCell}
        title={translate('facility')}
        headerClassName="left-most"
        width={facilityWidth}
      />
    ),
    ...quantityGridColumns,
    (
      <GridColumn
        key="buttons"
        headerCell={renderIconButtonsHeaderCell}
        headerClassName="Link"
        cell={renderIconButtonsCell}
        width={iconsWidth}
      />
    ),
  ];

  const onExpandRow = (dataItem: any) => setGridState({result: updateDataResult(gridState, dataItem)});

  return (
    <Column className="Legend Grouping-grid">
      <Grid
        data={gridState.result}
        groupable={true}
        cellRender={cellRender}
        headerCellRender={headerCellRender}
        rowRender={rowRenderer(onExpandRow, columnQuantities)}
        style={{...legendGridStyle, width}}
        {...state}
      >
        {gridContent}
      </Grid>
    </Column>
  );
};
