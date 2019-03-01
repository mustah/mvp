import {GridCellProps, GridColumn, GridRowProps} from '@progress/kendo-react-grid';
import {default as classNames} from 'classnames';
import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {IconRightArrow} from '../../../components/icons/IconRightArrow';
import {RowLeft, RowMiddle, RowRight} from '../../../components/layouts/row/Row';
import {InfoText, Medium as MediumText} from '../../../components/texts/Texts';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {colorOf} from './graphContentsMapper';
import {allQuantitiesMap, Medium, Quantity, toMediumText} from '../../../state/ui/graph/measurement/measurementModels';
import {OnClick, OnClickWith} from '../../../types/Types';
import {RowDispatch} from '../containers/LegendContainer';
import {ColumnQuantities, LegendType, MediumViewOptions, QuantityId, SelectedQuantityColumns} from '../reportModels';
import {isGroupHeader} from './measurementGridHelper';

interface CurrentLegendType {
  type: LegendType;
}

interface CurrentQuantity {
  quantity: Quantity;
}

export interface QuantityCell extends ColumnQuantities {
  selectedQuantityColumns: SelectedQuantityColumns;
  toggleQuantityById: OnClickWith<QuantityId>;
}

export interface RowProps extends ColumnQuantities, RowDispatch {
  onExpandRow: OnClick;
  mediumViewOptions: MediumViewOptions;
}

interface MediumTdProps {
  removeAll: OnClick;
  hideAll: OnClick;
  isAllHidden: boolean;
}

const MediumTd = ({hideAll, isAllHidden, removeAll}: MediumTdProps) => (
  <td className="check-box-td">
    <RowLeft>
      <RowRight title={firstUpperTranslated('hide all')}>
        <ButtonVisibility onClick={hideAll} checked={isAllHidden}/>
      </RowRight>
      <RowRight title={firstUpperTranslated('remove all')}>
        <ButtonDelete onClick={removeAll}/>
      </RowRight>
    </RowLeft>
  </td>
);

interface CheckboxProps {
  checked: boolean;
  disabled: boolean;
}

const checkboxPropsOf = (quantities: Quantity[], quantity: Quantity): CheckboxProps => {
  const checked = quantities.some(it => it === quantity);
  const disabled = !checked && quantities.length === 2;
  return {checked, disabled};
};

const renderGroupHeaderTds = ({
  columnQuantities,
  mediumViewOptions,
  removeAllByType,
  showHideAllByType,
  toggleQuantityByType,
  type,
}: RowProps & CurrentLegendType) => {
  const tds = columnQuantities.map((quantity) => {
    const key = `group-header-td-${type}-${quantity}`;
    if (allQuantitiesMap[type].some(q => q === quantity)) {
      const checkboxProps = checkboxPropsOf(mediumViewOptions[type].quantities, quantity);
      const {checked, disabled} = checkboxProps;
      const onClick = () => toggleQuantityByType({type, quantity});
      return (
        <td key={`group-header-td-${type}-${quantity}-${checked}-${disabled}`} className="check-box-td">
          <Checkbox {...checkboxProps} onCheck={onClick} iconStyle={{fill: colorOf(quantity)}}/>
        </td>
      );
    } else {
      return <td key={key}/>;
    }
  });

  const removeAll = () => removeAllByType(type);
  const hideAll = () => showHideAllByType(type);
  const isAllHidden = !(!mediumViewOptions[type].isAllLinesHidden);
  tds.push(
    <MediumTd
      key={`group-header-last-td-${type}-${isAllHidden}`}
      hideAll={hideAll}
      isAllHidden={isAllHidden}
      removeAll={removeAll}
    />);
  return tds;
};

export const getGroupHeaderTitle = (type: LegendType): string => toMediumText(type as Medium) || 'average';

const renderGroupHeader = (props: RowProps, dataItem: any) => {
  const type: LegendType = dataItem.value;
  const isVisible = dataItem.items[0].isRowExpanded;
  const onClick = () => props.onExpandRow(dataItem);
  return (
    <tr className="GroupHeader Foldable" key={`group-header-${type}`}>
      <td colSpan={2} key={`group-header-td-title-${type}`}>
        <RowMiddle onClick={onClick} className="clickable">
          <IconRightArrow className={classNames('Foldable-arrow', {isVisible})}/>
          <MediumText className="Bold">{firstUpperTranslated(getGroupHeaderTitle(type).toLowerCase())}</MediumText>
          <InfoText style={{marginLeft: 8, fontStyle: 'normal'}}>({dataItem.items.length})</InfoText>
        </RowMiddle>
      </td>
      {renderGroupHeaderTds({...props, type})}
    </tr>
  );
};

type TableRow = React.ReactElement<HTMLTableRowElement>;

const renderDataRow = (tr: TableRow, dataItem: any) => {
  const {isRowExpanded, last} = dataItem;
  return isRowExpanded
    ? last ? React.cloneElement(tr, {className: 'last'}, tr.props.children) : tr
    : null;
};

export const rowRenderer = (props: RowProps) =>
  (tr: TableRow, {dataItem, rowType}: GridRowProps) => {
    if (isGroupHeader(rowType)) {
      return renderGroupHeader(props, dataItem);
    } else {
      return renderDataRow(tr, dataItem);
    }
  };

const renderQuantityCell =
  ({quantity, selectedQuantityColumns, toggleQuantityById}: QuantityCell & CurrentQuantity) =>
    ({dataItem: {id, label, type, quantities}}: GridCellProps) => {
      if (allQuantitiesMap[type].some(q => q === quantity)) {
        const quantityColumns = selectedQuantityColumns[type];
        const checked = quantities.some(it => it === quantity);
        const disabled = !checked && quantityColumns.length === 2 && quantityColumns.some(it => it === quantity);
        const checkboxProps: CheckboxProps = {checked, disabled};
        const onClick = () => toggleQuantityById({id, quantity});
        return (
          <td key={`item-td-${type}-${quantity}-${checked}-${disabled}`}>
            <Checkbox {...checkboxProps} onCheck={onClick} iconStyle={{fill: colorOf(quantity)}}/>
          </td>);
      } else {
        return <td/>;
      }
    };

export const quantityColumnWidth = 76;

export const renderColumns = (props: QuantityCell): React.ReactNode[] =>
  props.columnQuantities.map(quantity => (
    <GridColumn
      key={`legend-columns-${quantity}`}
      title={`${translate(`${quantity} short`)}`}
      cell={renderQuantityCell({...props, quantity})}
      width={quantityColumnWidth}
    />)
  );
