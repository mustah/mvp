import * as React from 'react';
import {Callback} from '../types/Types';

export interface ExportToExcelProps {
  isExportingToExcel: boolean;
  exportToExcelSuccess: Callback;
  // TODO[!must!]: Our types for React's hooks are wrong. It is solved in the newest version of react.
  save: (exporter: React.Ref<{}>) => void;
}

export const useExportToExcel = ({isExportingToExcel, exportToExcelSuccess, save}: ExportToExcelProps) => {
  const exporter = React.useRef();
  React.useEffect(() => {
    if (isExportingToExcel) {
      save(exporter);
      exportToExcelSuccess();
    }
  }, [isExportingToExcel]);
  return exporter;
};
