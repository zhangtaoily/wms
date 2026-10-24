<template>
  <div class="sql-highlight-container" :class="{ disabled: disabled }">
    <pre class="sql-highlight-pre"><code class="sql" ref="highlightCode" v-html="highlightedText"></code></pre>
    <textarea
      ref="textarea"
      :value="value"
      @input="handleInput"
      @scroll="handleScroll"
      :disabled="disabled"
      class="sql-highlight-textarea"
      spellcheck="false"
    ></textarea>
  </div>
</template>

<script>
import hljs from "highlight.js/lib/core";
import sql from "highlight.js/lib/languages/sql";
import "highlight.js/styles/github.css";
hljs.registerLanguage("sql", sql);

export default {
  name: "SqlHighlightTextarea",
  props: {
    value: {
      type: String,
      default: ""
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      highlightedText: ""
    };
  },
  watch: {
    value: {
      handler(newVal) {
        this.highlightSql(newVal);
      },
      immediate: true
    }
  },
  mounted() {
    this.highlightSql(this.value);
  },
  methods: {
    highlightSql(text) {
      if (!text) {
        this.highlightedText = "";
        return;
      }
      try {
        const result = hljs.highlight(text, { language: "sql" });
        this.highlightedText = result.value;
      } catch (e) {
        this.highlightedText = text;
      }
      this.$nextTick(() => {
        this.syncScroll();
      });
    },
    handleInput(e) {
      this.$emit("input", e.target.value);
    },
    handleScroll() {
      this.syncScroll();
    },
    syncScroll() {
      const textarea = this.$refs.textarea;
      const pre = this.$refs.highlightCode?.parentElement;
      if (textarea && pre) {
        pre.scrollTop = textarea.scrollTop;
        pre.scrollLeft = textarea.scrollLeft;
      }
    }
  }
};
</script>

<style scoped lang="scss">
.sql-highlight-container {
  position: relative;
  width: 100%;
  height: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.sql-highlight-container.disabled {
  border-color: #dcdfe6;
  cursor: not-allowed;
}

.sql-highlight-pre {
  position: absolute;
  top: 0;
  left: 0;
  margin: 0;
  padding: 8px 12px;
  width: 100%;
  height: 100%;
  font-family: "Monaco", "Menlo", "Ubuntu Mono", "Consolas", monospace;
  font-size: 13px;
  font-weight: bold;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  overflow: auto;
  pointer-events: none;
  box-sizing: border-box;
  background: #ffffff;
}

.sql-highlight-container.disabled .sql-highlight-pre {
  background: #e4e7ed;
  opacity: 0.6;
}

.sql-highlight-textarea {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  padding: 8px 12px;
  font-family: "Monaco", "Menlo", "Ubuntu Mono", "Consolas", monospace;
  font-size: 13px;
  font-weight: bold;
  line-height: 1.5;
  color: transparent;
  background: transparent;
  border: none;
  outline: none;
  resize: none;
  box-sizing: border-box;
  white-space: pre-wrap;
  word-break: break-all;
}

.sql-highlight-textarea:focus {
  outline: none;
}

.sql-highlight-textarea::placeholder {
  color: #c0c4cc;
}

.sql-highlight-textarea:disabled {
  cursor: not-allowed;
}
</style>