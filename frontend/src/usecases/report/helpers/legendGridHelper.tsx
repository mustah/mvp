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
import {allQuantitiesMap, getGroupHeaderTitle, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {OnClick, OnClickEventHandler, OnClickWith} from '../../../types/Types';
import {RowDispatch} from '../containers/LegendContainer';
import {ColumnQuantities, LegendType, LegendViewOptions, QuantityId, SelectedQuantities} from '../reportModels';
import {colorFor} from './graphContentsMapper';
import {isGroupHeader} from './measurementGridHelper';

interface CurrentLegendType {
  type: LegendType;
}

interface CurrentQuantity {
  quantity: Quantity;
}

export interface QuantityCell extends ColumnQuantities {
  selectedQuantitiesMap: SelectedQuantities;
  toggleQuantityById: OnClickWith<QuantityId>;
}

export interface RowProps extends ColumnQuantities, RowDispatch {
  onExpandRow: OnClickEventHandler;
  mediumViewOptions: LegendViewOptions;
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
    if (allQuantitiesMap[type].indexOf(quantity) !== -1) {
      const checked = mediumViewOptions[type].quantities.indexOf(quantity) !== -1;
      const onClick = () => toggleQuantityByType({type, quantity});
      return (
        <td key={`group-header-td-${type}-${quantity}-${checked}`} className="check-box-td">
          <Checkbox checked={checked} onCheck={onClick} iconStyle={{fill: colorFor(quantity)}}/>
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

const renderGroupHeader = (props: RowProps, dataItem: any) => {
  const type: LegendType = dataItem.value;
  const isVisible = dataItem.items[0].isRowExpanded;
  const onClick = () => props.onExpandRow(dataItem);
  return (
    <tr className="GroupHeader Foldable" key={`group-header-${type}`}>
      <td colSpan={2} key={`group-header-td-title-${type}`}>
        <RowMiddle onClick={onClick} className="clickable">
          <IconRightArrow className={classNames('Foldable-arrow', {isVisible})}/>
          <MediumText className="Bold">{getGroupHeaderTitle(type)}</MediumText>
          <InfoText style={{marginLeft: 8, fontStyle: 'normal'}}>({dataItem.items.length})</InfoText>
        </RowMiddle>
      </td>
      {renderGroupHeaderTds({...props, type})}
    </tr>
  );
};

type TableRow = React.ReactElement<HTMLTableRowElement>;

const renderDataRow = (tr: TableRow, {isRowExpanded}: any) => isRowExpanded ? tr : null;

export const rowRenderer = (props: RowProps) =>
  (tr: TableRow, {dataItem, rowType}: GridRowProps) => {
    if (isGroupHeader(rowType)) {
      return renderGroupHeader(props, dataItem);
    } else {
      return renderDataRow(tr, dataItem);
    }
  };

const renderQuantityCell =
  ({quantity, selectedQuantitiesMap, toggleQuantityById}: QuantityCell & CurrentQuantity) =>
    ({dataItem: {id, label, type, quantities}}: GridCellProps) => {
      if (allQuantitiesMap[type].indexOf(quantity) !== -1) {
        const checked = quantities.indexOf(quantity) !== -1;
        const onClick = () => toggleQuantityById({id, quantity});
        return (
          <td key={`item-td-${type}-${quantity}-${checked}`}>
            <Checkbox checked={checked} onCheck={onClick} iconStyle={{fill: colorFor(quantity)}}/>
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
