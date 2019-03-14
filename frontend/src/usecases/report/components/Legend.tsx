import {DataResult, process, State} from '@progress/kendo-data-query';
import {Grid, GridCellProps, GridColumn} from '@progress/kendo-react-grid';
import Drawer from 'material-ui/Drawer';
import IconButton from 'material-ui/IconButton';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../app/routes';
import {borderRadius, drawerContainerStyle, gridStyle, iconStyle} from '../../../app/themes';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonInfo} from '../../../components/buttons/ButtonInfo';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {Column} from '../../../components/layouts/column/Column';
import {RowLeft, RowRight} from '../../../components/layouts/row/Row';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {DispatchToProps, OwnProps, StateToProps} from '../containers/LegendContainer';
import {QuantityCell, quantityColumnWidth, renderColumns, RowProps, rowRenderer} from '../helpers/legendGridHelper';
import {cellRender, headerCellRender} from '../helpers/measurementGridHelper';
import {isMedium, LegendType} from '../reportModels';
import './Legend.scss';
import NavigationClose from 'material-ui/svg-icons/navigation/close';

const legendGridStyle: React.CSSProperties = {
  ...gridStyle,
  borderTopLeftRadius: borderRadius,
  borderTopRightRadius: borderRadius,
  marginBottom: 24,
};

const renderLabel = ({id, label, type}: any) =>
  isMedium(type)
    ? (
      <Link to={`${routes.meter}/${id}`} className="link">
        <ButtonInfo
          label={orUnknown(label)}
          iconStyle={iconStyle}
          title={orUnknown(label).toString()}
        />
      </Link>)
    : orUnknown(label);

const renderLabelCell = ({dataItem: {id, label, city, address, type}}: GridCellProps) =>
  label
    ? (
      <td className="left-most first-uppercase" title={`${orUnknown(city)}, ${orUnknown(address)}`}>
        {renderLabel({id, label, type})}
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
  selectedQuantitiesMap,
  showHideLegend,
  showHideLegendRows,
  toggleLine,
  toggleQuantityByType,
  toggleQuantityById,
}: DispatchToProps & StateToProps & OwnProps) => {
  const columnRenderProps: QuantityCell = {
    columnQuantities,
    selectedQuantitiesMap,
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

  const facilityColumnWidth = 144;
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
      <IconButton onClick={showHideLegend} className={'close-button'}>
        <NavigationClose/>
      </IconButton>
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
