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
import {Medium} from '../../../state/ui/graph/measurement/measurementModels';
import {DispatchToProps, OwnProps, StateToProps} from '../containers/LegendContainer';
import {quantityColumnWidth, renderColumns, RowProps, rowRenderer} from '../helpers/legendGridHelper';
import {cellRender, headerCellRender} from '../helpers/measurementGridHelper';
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

const state: State = {group: [{field: 'medium'}]};

export const Legend = ({
  deleteItem,
  showHideAllByMedium,
  isVisible,
  legendItems,
  mediumViewOptions,
  removeAllByMedium,
  showHideLegend,
  showHideMediumRows,
  toggleLine,
  toggleQuantityByMedium,
  toggleQuantityById,
}: DispatchToProps & StateToProps & OwnProps) => {
  const [quantityGridColumns, columnQuantities] =
    React.useMemo(() => renderColumns(legendItems, toggleQuantityById), [legendItems]);

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
    onExpandRow: (dataItem: any) => showHideMediumRows(dataItem.value as Medium),
    columnQuantities,
    showHideAllByMedium,
    mediumViewOptions,
    removeAllByMedium,
    toggleQuantityByMedium,
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
