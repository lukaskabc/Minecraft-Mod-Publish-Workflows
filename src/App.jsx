import {useEffect, useMemo, useState} from "react";
import Form from "@rjsf/core";
import validator from "@rjsf/validator-ajv8";
import {CopyToClipboard} from 'react-copy-to-clipboard';
import { Light as SyntaxHighlighter } from 'react-syntax-highlighter';
import json from 'react-syntax-highlighter/dist/esm/languages/hljs/json.js';
import docco from 'react-syntax-highlighter/dist/esm/styles/hljs/docco';

SyntaxHighlighter.registerLanguage('json', json);

const uiSchema = {
  "ui:globalOptions": {
    "enableMarkdownInDescription": true
  }
}

export default function App() {

  const [schema, setSchema] = useState(null);
  const [loadError, setLoadError] = useState(null);
  const [formData, setFormData] = useState({});

  const [schemaUrl, setSchemaUrl] = useState("https://raw.githubusercontent.com/lukaskabc/Minecraft-Mod-Publish-Workflows/refs/heads/main/publish.config.schema.json")

  useEffect(() => {
    let cancelled = false;
    if (schemaUrl == null || schemaUrl.trim() === "") {
      return
    }

    setSchema(null)
    setLoadError(null)

    fetch(schemaUrl)
      .then((res) => {
        if (!res.ok)
          throw new Error(`Failed to fetch schema: HTTP ${res.status}`);
        return res.json();
      })
      .then((json) => {
        if (!cancelled) setSchema(json);
      })
      .catch((err) => {
        if (!cancelled) setLoadError(err.message);
      });

    return () => {
      cancelled = true;
    };
  }, [schemaUrl]);

  const customButtonTemplates = {
    AddButton: (props) => (
      <button
        {...props}
      >
        Add
      </button>
    ),
    RemoveButton: (props) => (
      <button
        {...props}
      >
        Delete
      </button>
    ),
    MoveUpButton: (props) => (
      <button
        {...props}
      >
        ↑
      </button>
    ),
    MoveDownButton: (props) => (
      <button
        {...props}
      >
        ↓
      </button>
    ),
    SubmitButton: () => null
  };

  const jsonResult = useMemo(() => {
    return JSON.stringify(formData, null, 2)
  }, [formData]);

  return (
    <div className="app-shell">
      <div className="schema-source">
        Schema loaded from:
        <input
            type={"text"}
            value={schemaUrl}
            onChange={e => setSchemaUrl(e.target.value)}
            className="p-inputtext p-component"
            style={{ flex: 1, padding: '0.5rem', width: 'auto', maxWidth: '100%' }}
        />
      </div>

      {loadError && (
        <p className={'error-message'}>
          {`Could not load schema: ${loadError}`}
        </p>
      )}

      {!schema && !loadError && (
        <div className="loading">
          <span>Loading schema…</span>
        </div>
      )}

      {schema && (
        <main className="content-grid">
          <Form
            schema={schema}
            uiSchema={uiSchema}
            validator={validator}
            formData={formData}
            onChange={(e) => setFormData(e.formData)}
            onSubmit={(e) => setFormData(e.formData)}
            templates={{ ButtonTemplates: customButtonTemplates }}
            liveValidate

          />

          <div>
            <SyntaxHighlighter language={"json"} style={docco}>
              {jsonResult}
            </SyntaxHighlighter>
            <CopyToClipboard text={jsonResult}>
              <button>Copy to clipboard</button>
            </CopyToClipboard>
          </div>
        </main>
      )}
    </div>
  );
}
