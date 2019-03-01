import {DataResult, process, State} from '@progress/kendo-data-query';
import {Grid, GridCellProps, GridColumn} from '@progress/kendo-react-grid';
import Drawer from 'material-ui/Drawer';
import * as React from 'react';
import {borderRadius, drawerContainerStyle, gridStyle} from '../../../app/themes';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {Column} from '../../../components/layouts/column/Column';
import {RowLeft, RowRight} from '../../../components/layouts/row/Row';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {DispatchToProps, OwnProps, StateToProps} from '../containers/LegendContainer';
import {QuantityCell, quantityColumnWidth, renderColumns, RowProps, rowRenderer} from '../helpers/legendGridHelper';
import {cellRender, headerCellRender} from '../helpers/measurementGridHelper';
import {LegendType} from '../reportModels';
import './Legend.scss';

const legendGridStyle: React.CSSProperties = {
  ...gridStyle,
  borderTopLeftRadius: borderRadius,
  borderTopRightRadius: borderRadius,
  marginBottom: 24,
};

const renderLabelCell = ({dataItem: {label, city, address}}: GridCellProps) =>
  label
    ? (
      <td className="left-most first-uppercase" title={`${orUnknown(city)}, ${orUnknown(address)}`}>
        {orUnknown(label)}
      </td>
    )
    : <td>-</td>;

const state: State = {group: [{field: 'type'}]};

export const Legend = ({
  columnQuantities,
  deleteItem,
  showHideAllByType,
  isVisible,
  legendItems,
  mediumViewOptions,
  removeAllByType,
  selectedQuantityColumns,
  showHideLegend,
  showHideLegendRows,
  toggleLine,
  toggleQuantityByType,
  toggleQuantityById,
}: DispatchToProps & StateToProps & OwnProps) => {
  const columnRenderProps: QuantityCell = {
    columnQuantities,
    selectedQuantityColumns,
    toggleQuantityById
  };
  const quantityGridColumns = React.useMemo(() => renderColumns(columnRenderProps), [legendItems]);
  const dataResult: DataResult = React.useMemo(() => process(legendItems, state), [legendItems]);

  const renderIconButtonsCell = ({dataItem: {id, isHidden}}: GridCellProps) => {
    const onDeleteItem = () => deleteItem(id);
    const onToggleItem = () => toggleLine(id);
    return (
      <td className="icons">
        <RowLeft>
          <RowRight>
            <ButtonVisibility key={`checked-${id}-${isHidden}`} onClick={onToggleItem} checked={isHidden}/>
          </RowRight>
          <RowRight>
            <ButtonDelete key={`delete-item-${id}`} onClick={onDeleteItem}/>
          </RowRight>
        </RowLeft>
      </td>
    );
  };

  const facilityColumnWidth = 138;
  const width = facilityColumnWidth + quantityColumnWidth + (columnQuantities.length * quantityColumnWidth) + 34;

  const gridContent: React.ReactNode[] = [
    (
      <GridColumn
        key="facility"
        cell={renderLabelCell}
        title={translate('facility')}
        headerClassName="left-most"
        width={facilityColumnWidth}
      />
    ),
    ...quantityGridColumns,
    (
      <GridColumn
        key="buttons"
        headerClassName="Link"
        cell={renderIconButtonsCell}
        width={quantityColumnWidth}
      />
    ),
  ];

  const rowRenderProps: RowProps = {
    columnQuantities,
    mediumViewOptions,
    onExpandRow: (dataItem: any) => showHideLegendRows(dataItem.value as LegendType),
    removeAllByType,
    showHideAllByType,
    toggleQuantityByType,
  };

  return (
    <Drawer
      containerStyle={drawerContainerStyle}
      docked={false}
      openSecondary={true}
      open={isVisible}
      onRequestChange={showHideLegend}
      width={width}
      overlayStyle={{backgroundColor: 'transparent'}}
    >
      <Column className="Legend Grouping-grid">
        <Grid
          data={dataResult}
          groupable={true}
          cellRender={cellRender}
          headerCellRender={headerCellRender}
          rowRender={rowRenderer(rowRenderProps)}
          style={{...legendGridStyle, width}}
          {...state}
        >
          {gridContent}
        </Grid>
      </Column>
    </Drawer>
  );
};
