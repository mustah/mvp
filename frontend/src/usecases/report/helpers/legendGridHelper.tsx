import {GridCellProps, GridColumn, GridRowProps} from '@progress/kendo-react-grid';
import {default as classNames} from 'classnames';
import {toArray} from 'lodash';
import Checkbox from 'material-ui/Checkbox';
import * as React from 'react';
import {ButtonDelete} from '../../../components/buttons/ButtonDelete';
import {ButtonVisibility} from '../../../components/buttons/ButtonVisibility';
import {IconRightArrow} from '../../../components/icons/IconRightArrow';
import {RowLeft, RowMiddle, RowRight} from '../../../components/layouts/row/Row';
import {InfoText, Medium as MediumText} from '../../../components/texts/Texts';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {colorizeMeters} from '../../../state/ui/graph/measurement/graphContentsMapper';
import {allQuantities, Medium, Quantity, toMediumText} from '../../../state/ui/graph/measurement/measurementModels';
import {Dictionary, OnClick, OnClickWith} from '../../../types/Types';
import {LegendItem, MediumViewOptions, QuantityId, QuantityMedium, SelectedQuantityColumns} from '../reportModels';
import {isGroupHeader} from './measurementGridHelper';

interface CurrentMedium {
  medium: Medium;
}

interface CurrentQuantity {
  quantity: Quantity;
}

interface QuantityCell {
  selectedQuantityColumns: SelectedQuantityColumns;
  toggleQuantityById: OnClickWith<QuantityId>;
}

export interface ColumnRenderProps extends QuantityCell {
  legendItems: LegendItem[];
}

export interface RowProps {
  columnQuantities: Quantity[];
  onExpandRow: OnClick;
  mediumViewOptions: MediumViewOptions;
  removeAllByMedium: OnClickWith<Medium>;
  showHideAllByMedium: OnClickWith<Medium>;
  toggleQuantityByMedium: OnClickWith<QuantityMedium>;
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
  const checked = quantities.indexOf(quantity) !== -1;
  const disabled = !checked && quantities.length === 2;
  return {checked, disabled};
};

const renderGroupHeaderTds = ({
  columnQuantities,
  medium,
  mediumViewOptions,
  removeAllByMedium,
  showHideAllByMedium,
  toggleQuantityByMedium
}: RowProps & CurrentMedium) => {
  const tds = columnQuantities.map((quantity) => {
    const key = `group-header-td-${medium}-${quantity}`;
    if (allQuantities[medium].some(q => q === quantity)) {
      const checkboxProps = checkboxPropsOf(mediumViewOptions[medium].quantities, quantity);
      const {checked, disabled} = checkboxProps;
      const onClick = () => toggleQuantityByMedium({medium, quantity});
      return (
        <td key={`group-header-td-${medium}-${quantity}-${checked}-${disabled}`} className="check-box-td">
          <Checkbox {...checkboxProps} onCheck={onClick} iconStyle={{fill: colorizeMeters(quantity)}}/>
        </td>
      );
    } else {
      return <td key={key}/>;
    }
  });

  const removeAll = () => removeAllByMedium(medium);
  const hideAll = () => showHideAllByMedium(medium);
  const isAllHidden = !(!mediumViewOptions[medium].isAllLinesHidden);
  tds.push(
    <MediumTd
      key={`group-header-last-td-${medium}-${isAllHidden}`}
      hideAll={hideAll}
      isAllHidden={isAllHidden}
      removeAll={removeAll}
    />);
  return tds;
};

const renderGroupHeader = (props: RowProps, dataItem: any) => {
  const medium: Medium = dataItem.value;
  const isVisible = dataItem.items[0].isRowExpanded;
  const onClick = () => props.onExpandRow(dataItem);
  return (
    <tr className="GroupHeader Foldable" key={`group-header-${medium}`}>
      <td colSpan={2} key={`group-header-td-title-${medium}`}>
        <RowMiddle onClick={onClick} className="clickable">
          <IconRightArrow className={classNames('Foldable-arrow', {isVisible})}/>
          <MediumText className="Bold">{firstUpperTranslated(toMediumText(medium).toLowerCase())}</MediumText>
          <InfoText style={{marginLeft: 8, fontStyle: 'normal'}}>({dataItem.items.length})</InfoText>
        </RowMiddle>
      </td>
      {renderGroupHeaderTds({...props, medium})}
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

const renderQuantityCell = ({quantity, selectedQuantityColumns, toggleQuantityById}: QuantityCell & CurrentQuantity) =>
  ({dataItem: {id, label, medium, quantities}}: GridCellProps) => {
    if (allQuantities[medium].some(q => q === quantity)) {
      const quantityColumns = selectedQuantityColumns[medium];
      const checked = quantities.indexOf(quantity) !== -1;
      const disabled = !checked && quantityColumns.length === 2 && quantityColumns.indexOf(quantity) === -1;
      const checkboxProps: CheckboxProps = {checked, disabled};
      const onClick = () => toggleQuantityById({id, quantity});
      return (
        <td key={`item-td-${medium}-${quantity}-${checked}-${disabled}`}>
          <Checkbox {...checkboxProps} onCheck={onClick} iconStyle={{fill: colorizeMeters(quantity)}}/>
        </td>);
    } else {
      return <td/>;
    }
  };

export const quantityColumnWidth = 76;

export const renderColumns =
  ({legendItems, ...otherProps}: ColumnRenderProps): [React.ReactNode[], Quantity[]] => {
    const columns: Dictionary<React.ReactNode> = {};
    const columnQuantities: Quantity[] = [];

    legendItems.forEach(({id, label, medium}: LegendItem) => {
      allQuantities[medium].forEach((quantity) => {
        if (columns[quantity] === undefined) {
          columns[quantity] = (
            <GridColumn
              key={`legend-${id}-${label}-${medium}-${quantity}`}
              title={`${translate(`${quantity} short`)}`}
              cell={renderQuantityCell({...otherProps, quantity})}
              width={quantityColumnWidth}
            />);
          columnQuantities.push(quantity);
        }
      });
    });

    return [toArray(columns), columnQuantities];
  };
